#include "org_gearvrf_sample_vropencv_StereoViewManager.h"
#include <vector>
#include <cmath>
#include <pthread.h>
#include <opencv2/opencv.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <android/log.h>
#include <jni.h>
#include <stdio.h>
#include <OpenNI.h>
#include <sys/mman.h>
#include <unistd.h>
#include "OniSampleUtilities.h"

#define APPNAME "JAI_ASUS_NDK"

#define SAMPLE_READ_WAIT_TIMEOUT 2000 //2000ms

using namespace cv;
using namespace std;
using namespace openni;

static JavaVM* jvm = 0;
JNIEnv* env_process;
jmethodID methodID, methodID_face;
static jobject activity = 0; // GlobalRef
Mat depthImg;
Mat rgbImg;
bool isKinectRunning(false);
pthread_mutex_t frameLocker;

#define KINECT_OFF 0
#define KINECT_STEREO 1
#define KINECT_NOSTEREO 2
#define KINECT_THRESHOLD 3

class DepthSensor {
public:
	void depthNormalize(void);
	void depthInpaint(void);
	void createRightView(void);
	void showDepthImage(void);
	void faceDetect(void);
	void setMode(int);
	void adaptiveThresholding(void);
	void localize(void);
	int getMode(void);
	int getWidth(void);
	int getHeight(void);
	void setKeyframe(bool);
	void keyFrameLocalize(void);
	Mat extractSurfaceNormalCenter(void);
	DepthSensor();
	~DepthSensor();
	Mat depthMap, depthMap_8b, colorMap, left_color, right_color;
	VideoStream depth, color;
	VideoStream** m_streams;
	Mat T_global, _T;
	Mat keyframe, depthMap_keyframe;
private:
	void sendCallbackRt(Mat&, Mat&);
	void sendFaceDetectCallback(float x, float y, float z);
	Device device;
	VideoMode depthVideoMode, colorVideoMode;
	int mode;
	double _min, _max;
	int width, height;
	CascadeClassifier cascade;
	vector<Rect> faces;
	bool isKeyFrame;
	vector<KeyPoint> keyframe_kp;
	Mat keyframe_desc;
};

DepthSensor* mDepthSensor = NULL;

DepthSensor::DepthSensor() :
		width(0), height(0), _min(0), _max(0), mode(0), isKeyFrame(true) {
	int res_mode = 0;
	Status rc = OpenNI::initialize();
	if (rc != STATUS_OK) {
		__android_log_print(ANDROID_LOG_VERBOSE, APPNAME,
				"Initialize failed\n%s\n", OpenNI::getExtendedError());
	}
	rc = device.open(ANY_DEVICE);
	if (rc != openni::STATUS_OK) {
		__android_log_print(ANDROID_LOG_VERBOSE, APPNAME,
				"SimpleViewer: Device open failed:\n%s\n",
				OpenNI::getExtendedError());
		OpenNI::shutdown();
	}
	rc = depth.create(device, openni::SENSOR_DEPTH);
	if (rc == STATUS_OK) {
		const Array<VideoMode> *depthVideoModes;
		depthVideoModes = &(depth.getSensorInfo().getSupportedVideoModes());
		depthVideoMode = depth.getVideoMode();
		depthVideoMode.setResolution(
				(*depthVideoModes)[res_mode].getResolutionX(),
				(*depthVideoModes)[res_mode].getResolutionY());
		depthVideoMode.setFps((*depthVideoModes)[res_mode].getFps());
		width = (*depthVideoModes)[res_mode].getResolutionX();
		height = (*depthVideoModes)[res_mode].getResolutionY();
		depth.setVideoMode(depthVideoMode);
		rc = depth.start();
		depth.setMirroringEnabled(!depth.getMirroringEnabled());
		if (rc != STATUS_OK) {
			__android_log_print(ANDROID_LOG_VERBOSE, APPNAME,
					"SimpleViewer: Couldn't start depth stream:\n%s\n",
					OpenNI::getExtendedError());
			depth.destroy();
		}
	} else {
		__android_log_print(ANDROID_LOG_VERBOSE, APPNAME,
				"SimpleViewer: Couldn't find depth stream:\n%s\n",
				OpenNI::getExtendedError());
	}

	rc = color.create(device, SENSOR_COLOR);
	if (rc == STATUS_OK) {
		const Array<VideoMode> *colorVideoModes;
		colorVideoModes = &(color.getSensorInfo().getSupportedVideoModes());
		colorVideoMode = color.getVideoMode();
		colorVideoMode.setResolution(
				(*colorVideoModes)[res_mode].getResolutionX(),
				(*colorVideoModes)[res_mode].getResolutionY());
		colorVideoMode.setFps((*colorVideoModes)[res_mode].getFps());
		color.setVideoMode(colorVideoMode);
		width = (*colorVideoModes)[res_mode].getResolutionX();
		height = (*colorVideoModes)[res_mode].getResolutionY();
		rc = color.start();
		color.setMirroringEnabled(!color.getMirroringEnabled());
		if (rc != STATUS_OK) {
			__android_log_print(ANDROID_LOG_VERBOSE, APPNAME,
					"SimpleViewer: Couldn't start color stream:\n%s\n",
					OpenNI::getExtendedError());
			color.destroy();
		}
	} else {
		__android_log_print(ANDROID_LOG_VERBOSE, APPNAME,
				"SimpleViewer: Couldn't find color stream:\n%s\n",
				OpenNI::getExtendedError());
	}
	if (!depth.isValid() || !color.isValid()) {
		__android_log_print(ANDROID_LOG_VERBOSE, APPNAME,
				"SimpleViewer: No valid streams. Exiting\n");
		OpenNI::shutdown();
	}

	cascade.load("/sdcard/openni/haarcascade_frontalface_alt.xml");

	m_streams = new VideoStream*[2];
	m_streams[0] = &depth;
	m_streams[1] = &color;
}

DepthSensor::~DepthSensor() {
	depth.stop();
	depth.destroy();
	device.close();
	OpenNI::shutdown();
}

void DepthSensor::depthNormalize(void) {
	if (depthMap.empty() || colorMap.empty() || depthMap_8b.empty()) {
		//__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s : E return\n",__func__);
		return;
	}
	Point min_loc, max_loc;
	minMaxLoc(depthMap, &_min, &_max, &min_loc, &max_loc);
	depthMap.convertTo(depthMap_8b, CV_8UC1, 255.0 / (_max));
	return;
}

void DepthSensor::depthInpaint(void) {
	if (depthMap.empty() || colorMap.empty() || depthMap_8b.empty()) {
		//__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s : E return\n",__func__);
		return;
	}
	Mat small_depth, _tmp, _tmp1;
	Mat small_mask, mask;
	resize(depthMap_8b, small_depth, Size(), 0.2, 0.2);
	compare(small_depth,
			Mat::zeros(small_depth.rows, small_depth.cols, small_depth.type()),
			small_mask, CMP_EQ);
	compare(depthMap_8b,
			Mat::zeros(depthMap_8b.rows, depthMap_8b.cols, depthMap_8b.type()),
			mask, CMP_EQ);
	inpaint(small_depth, small_mask, _tmp1, 5.0, INPAINT_TELEA);
	resize(_tmp1, _tmp, depthMap_8b.size());
	_tmp.copyTo(depthMap_8b, mask);
	return;
}

void DepthSensor::createRightView() {
	if (depthMap.empty() || colorMap.empty() || depthMap_8b.empty()) {
		//__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s : E return\n",__func__);
		return;
	}
	vector<Mat> right_RGB;
	vector<Mat> RGB;
	Mat tmp;
	pthread_mutex_lock(&frameLocker);
	right_color.copyTo(tmp);
	pthread_mutex_unlock(&frameLocker);
	split(colorMap, RGB);
	split(tmp, right_RGB);
	right_RGB[0].setTo(0);
	right_RGB[1].setTo(0);
	right_RGB[2].setTo(0);
	for (int y = 0; y < height; y++) {
		for (int x = 0; x < width; x++) {
			double pos = double(x)
					- 200.0 * 62.0 * 255.0
							/ (double(depthMap_8b.at<uchar>(y, x)) * _max); // f*B/Z
			if (pos > 0 && pos < tmp.cols) {
				right_RGB[0].at<uchar>(y, int(pos)) = RGB[0].at<uchar>(y, x);
				right_RGB[1].at<uchar>(y, int(pos)) = RGB[1].at<uchar>(y, x);
				right_RGB[2].at<uchar>(y, int(pos)) = RGB[2].at<uchar>(y, x);
			} else {
				right_RGB[0].at<uchar>(y, x) = 0;
				right_RGB[1].at<uchar>(y, x) = 0;
				right_RGB[2].at<uchar>(y, x) = 0;
			}
		}
	}

	merge(right_RGB, tmp);
	Mat mask, gray;
	cvtColor(tmp, gray, COLOR_RGB2GRAY);
	compare(gray, Mat::zeros(gray.rows, gray.cols, gray.type()), mask, CMP_EQ);
	for (int y = 0; y < height; y++) {
		for (int x = width - 1; x >= 0; x--) {
			if (mask.at<uchar>(y, x) == 255 && x != 0 && x != width - 1) {
				right_RGB[0].at<uchar>(y, x) = right_RGB[0].at<uchar>(y, x + 1);
				right_RGB[1].at<uchar>(y, x) = right_RGB[1].at<uchar>(y, x + 1);
				right_RGB[2].at<uchar>(y, x) = right_RGB[2].at<uchar>(y, x + 1);
			}
		}
	}
	pthread_mutex_lock(&frameLocker);
	merge(right_RGB, right_color);
	pthread_mutex_unlock(&frameLocker);
	return;
}

void DepthSensor::showDepthImage(void) {
	if (depthMap.empty() || colorMap.empty() || depthMap_8b.empty()) {
		//__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s : E return\n",__func__);
		return;
	}
	vector<Mat> RGB;
	split(right_color, RGB);
	depthMap_8b.copyTo(RGB[0]);
	depthMap_8b.copyTo(RGB[1]);
	depthMap_8b.copyTo(RGB[2]);
	merge(RGB, right_color);
}

void DepthSensor::faceDetect(void) {
	if (depthMap.empty() || colorMap.empty() || depthMap_8b.empty()) {
		return;
	}

	Mat gray;
	cvtColor(colorMap, gray, COLOR_RGB2GRAY);
	equalizeHist(gray, gray);
	cascade.detectMultiScale(gray, faces, 1.1, 2, 0 | CASCADE_SCALE_IMAGE,
			Size(30, 30));
	if(faces.size() < 1) {
		return;
	}
	Rect r = faces[0];
	float scale = 1.f;
	float f_depth = 285.1711120605469;
	float cx = 159.5;
	float cy = 119.5;
	float fx = float((r.x + r.width/2.f - 1.f) * scale);
	float fy = float((r.y + 10 + r.height/2.f - 1.f) * scale);
	float z_ = float(depthMap_8b.at<uchar>(fy, fx) * _max / 255.0f);
	float face_x = 0.001f * (fx - cx) * z_ / f_depth;
	float face_y = -0.001f * (fy - cy) * z_ / f_depth;
	float face_z = -0.001f * z_;
	sendFaceDetectCallback(face_x, face_y, face_z);

	return;
}

void DepthSensor::sendFaceDetectCallback(float x, float y, float z) {
	jfloatArray facePos = env_process->NewFloatArray(3);
	jfloat* tmp = new jfloat[3];
	tmp[0] = x;
	tmp[1] = y;
	tmp[2] = z;
	env_process->SetFloatArrayRegion(facePos, 0, 3, tmp);
	env_process->CallVoidMethod(activity, methodID_face, facePos);
	env_process->ReleaseFloatArrayElements(facePos, tmp, 0);
	env_process->DeleteLocalRef(facePos);
	//usleep(30*1000);

}

void DepthSensor::adaptiveThresholding(void) {
	if (colorMap.empty() || depthMap.empty()) {
		return;
	}
	Mat gray, right_gray;
	cvtColor(colorMap, gray, COLOR_RGB2GRAY);
	medianBlur(gray, gray, 5);
	Canny(gray, gray, 100, 200);
	/*	adaptiveThreshold(gray, gray, 255, ADAPTIVE_THRESH_GAUSSIAN_C,
	 THRESH_BINARY_INV, 5, 2);*/
	gray.copyTo(right_gray);
	right_gray.setTo(0);

	for (int y = 0; y < height; y++) {
		for (int x = 0; x < width; x++) {
			double pos = double(x)
					- 200.0 * 62.0 * 255.0
							/ (double(depthMap_8b.at<uchar>(y, x)) * _max); // f*B/Z
			if (pos > 0 && pos < width && gray.at<uchar>(y, x) == 255) {
				right_gray.at<uchar>(y, int(pos)) = 255;
				right_gray.at<uchar>(y, int(pos)) = 255;
				right_gray.at<uchar>(y, int(pos)) = 255;
			}
		}
	}
	pthread_mutex_lock(&frameLocker);
	cvtColor(gray, left_color, COLOR_GRAY2RGB);
	cvtColor(right_gray, right_color, COLOR_GRAY2RGB);
	pthread_mutex_unlock(&frameLocker);
}

int DepthSensor::getWidth(void) {
	return width;
}

int DepthSensor::getHeight(void) {
	return height;
}

int DepthSensor::getMode(void) {
	return this->mode;
}

void DepthSensor::setMode(int mod) {
	this->mode = mod;
	return;
}

void DepthSensor::setKeyframe(bool status) {
	this->isKeyFrame = status;
}

void DepthSensor::localize(void) {
	jfloatArray result = env_process->NewFloatArray(16);
	env_process->CallVoidMethod(activity, methodID, result);
	jfloat* tmp = env_process->GetFloatArrayElements(result, 0);
	if (T_global.empty()) {
		T_global.create(4, 4, CV_64F);
		_T.create(4, 4, CV_64F);
		for (int i = 0; i < 16; i++) {
			int y = i / 4;
			int x = i % 4;
			T_global.at<float>(y, x) = tmp[i];
		}
		T_global.copyTo(_T);
	} else {
		for (int i = 0; i < 16; i++) {
			int y = i / 4;
			int x = i % 4;
			_T.at<float>(y, x) = tmp[i];
		}
	}
	env_process->ReleaseFloatArrayElements(result, tmp, 0);
	env_process->DeleteLocalRef(result);
}

void DepthSensor::keyFrameLocalize(void) {
	// create ORB features
	if (colorMap.empty() || depthMap.empty()) {
		return;
	}

	Ptr<ORB> orb = ORB::create();
	orb->setMaxFeatures(1000);
	vector<KeyPoint> kp;
	Mat desc;

	if (isKeyFrame) {
		Mat mask;
		compare(depthMap, Mat::zeros(height, width, CV_16UC1), mask, CMP_NE);
		orb->detectAndCompute(colorMap, mask, keyframe_kp, keyframe_desc);
		colorMap.copyTo(keyframe);
		depthMap.copyTo(depthMap_keyframe);
		isKeyFrame = false;
		return;
	} else {
		orb->detectAndCompute(colorMap, noArray(), kp, desc);
	}

	Ptr<DescriptorMatcher> matcher = DescriptorMatcher::create(
			"BruteForce-Hamming");
	vector<DMatch> matches;
	if (kp.empty()) {
		return;
	}
	matcher->match(keyframe_desc, desc, matches);

	double max_dist = 0;
	double min_dist = 10000;
	for (int i = 0; i < keyframe_desc.rows; i++) {
		double dist = matches[i].distance;
		if (dist < min_dist)
			min_dist = dist;
		if (dist > max_dist)
			max_dist = dist;
	}

	vector<Point2f> points2d;
	vector<Point3f> points3d;
	std::vector<DMatch> good_matches;
	float f_depth = 285.1711120605469;
	float cx = 159.5;
	float cy = 119.5;
	for (int i = 0; i < keyframe_desc.rows; i++) {
		if (matches[i].distance <= max(2 * min_dist, 0.5 * max_dist)) {
			good_matches.push_back(matches[i]);
			float Z = depthMap_keyframe.at<uint16_t>(keyframe_kp[i].pt);
			Point3f pt;
			pt.x = (keyframe_kp[i].pt.x - cx) * Z / f_depth;
			pt.y = (keyframe_kp[i].pt.y - cy) * Z / f_depth;
			pt.z = Z;
			points3d.push_back(pt);
			int dest_idx = matches[i].trainIdx;
			points2d.push_back(kp[dest_idx].pt);
		}
	}

	if (good_matches.size() < 4) {
		cout << "Less than 4 good matches" << endl;
		return;
	}
	int iterationsCount = 500;        // number of Ransac iterations.
	float reprojectionError = 2.0; // maximum allowed distance to consider it an inlier.
	float confidence = 0.95;
	//Mat rvec, tvec;
	Mat _A_matrix = cv::Mat::zeros(3, 3, CV_64FC1); // intrinsic camera parameters
	_A_matrix.at<double>(0, 0) = 285.1711120605469;       //      [ fx   0  cx ]
	_A_matrix.at<double>(1, 1) = 285.1711120605469;       //      [  0  fy  cy ]
	_A_matrix.at<double>(0, 2) = 159.5;       //      [  0   0   1 ]
	_A_matrix.at<double>(1, 2) = 119.5;
	_A_matrix.at<double>(2, 2) = 1;
	cv::Mat distCoeffs = cv::Mat::zeros(4, 1, CV_64FC1); // vector of distortion coefficients
	cv::Mat rvec = cv::Mat::zeros(3, 1, CV_64FC1);     // output rotation vector
	cv::Mat tvec = cv::Mat::zeros(3, 1, CV_64FC1);  // output translation vector
	bool useExtrinsicGuess = false;

	solvePnPRansac(Mat(points3d), Mat(points2d), _A_matrix, distCoeffs, rvec,
			tvec, useExtrinsicGuess, iterationsCount, reprojectionError,
			confidence);
	Mat R;
	Rodrigues(rvec, R); // R is 3x3
	sendCallbackRt(R, tvec);
}

void DepthSensor::sendCallbackRt(Mat& R, Mat& tvec) {
	jfloatArray result = env_process->NewFloatArray(16);
	jfloat* tmp = new jfloat[16];
	tmp[0] = float(R.at<double>(0, 0));
	tmp[1] = float(R.at<double>(0, 1));
	tmp[2] = float(R.at<double>(0, 2));
	tmp[3] = float(tvec.at<double>(0, 0) * 0.01);
	tmp[4] = float(R.at<double>(1, 0));
	tmp[5] = float(R.at<double>(1, 1));
	tmp[6] = float(R.at<double>(1, 2));
	tmp[7] = float(tvec.at<double>(1, 0) * 0.01);
	tmp[8] = float(R.at<double>(2, 0));
	tmp[9] = float(R.at<double>(2, 1));
	tmp[10] = float(R.at<double>(2, 2));
	tmp[11] = float(tvec.at<double>(2, 0) * 0.01);
	tmp[12] = 0.f;
	tmp[13] = 0.f;
	tmp[14] = 0.f;
	tmp[15] = 1.f;
	env_process->SetFloatArrayRegion(result, 0, 16, tmp);
	env_process->CallVoidMethod(activity, methodID, result);
	env_process->ReleaseFloatArrayElements(result, tmp, 0);
	env_process->DeleteLocalRef(result);
}

Mat DepthSensor::extractSurfaceNormalCenter(void) {
	Mat T_ = Mat::eye(4, 4, CV_32F);
	if (depthMap.empty() || colorMap.empty()) {
		return T_;
	}
	float f_depth = 285.1711120605469;
	float cx = 159.5;
	float cy = 119.5;
	int arr[] = { -9, -6, -3, -1, 0, 1, 3, 6, 9 };
	int arr_size = 9;
	int x = width / 2;
	int y = height / 2;

	float Z = depthMap_8b.at<uchar>(y, x) * _max / 255.0;

	vector<Vec4d> _normalPts;
	for (int dy = 0; dy < arr_size; dy++) {
		for (int dx = 0; dx < arr_size; dx++) {
			int idx = (x + arr[dx]) + width * (y + arr[dy]);
			float z_ = depthMap_8b.at<uchar>(y + arr[dy], x + arr[dx]) * _max
					/ 255.0;
			if (abs(z_ - Z) < Z * 0.05 && (x + arr[dx] > 0)
					&& (x + arr[dx] < width) && (y + arr[dy] > 0)
					&& (y + arr[dy] < height))
				_normalPts.push_back(
						Vec4f((x + arr[dx] - cx) * z_ / f_depth,
								-1.f * (y + arr[dy] - cy) * z_ / f_depth,
								-1.f * z_, 1.f));
		}
	}

	Vec3f n;
	if (_normalPts.size() < 3 || Z == 0.f) {
		return T_;
	} else {
		Mat A = Mat(_normalPts).reshape(1, _normalPts.size());
		Mat B = A.t() * A;
		Mat eig, eigvec;
		eigen(B, eig, eigvec);
		Vec3f d(float(eigvec.at<double>(3, 0)), float(eigvec.at<double>(3, 1)),
				float(eigvec.at<double>(3, 2)));
		n = normalize(d);
	}

	Vec3f xtemp, x1, y1, z1;
	y1 = n;
	xtemp = Vec3f(1.f, 0.f, 0.f);
	Vec3f val;
	val = xtemp.cross(y1);
	if (val.val[0] == 0.f && val.val[1] == 0.f && val.val[2] == 0.f) {
		// if the vectors are collinear then perturb the vector
		xtemp = Vec3f(1.f, 0.1f, 0.f);
		val = xtemp.cross(y1);
	}
	z1 = val;
	z1 = normalize(z1);
	x1 = y1.cross(z1);
	Mat T;
	hconcat(x1, y1, T);
	hconcat(T, z1, T);
	float z_ = depthMap_8b.at<uchar>(y, x) * _max / 255.0;
	hconcat(T,
			Vec3f(0.001f * (x - cx) * z_ / f_depth,
					-0.001f * (y - cy) * z_ / f_depth, -0.001f * z_), T);
	vconcat(T, Mat(Vec4f(0.f, 0.f, 0.f, 1.f)).t(), T);
	T = T.t();
	T.copyTo(T_);
	__android_log_print(ANDROID_LOG_VERBOSE, APPNAME,
			"extractSurfaceNormalCenter()\n");
	return T;
}

void *processImage(void* arg) {
	jvm->AttachCurrentThread(&env_process, NULL);
	jclass clazz = env_process->GetObjectClass(activity);
	methodID = env_process->GetMethodID(clazz, "CallbackfromJNI", "([F)V");
	methodID_face = env_process->GetMethodID(clazz, "CallbackFacefromJNI",
			"([F)V");
	DepthSensor* mySensor = (DepthSensor *) arg;
	__android_log_print(ANDROID_LOG_VERBOSE, APPNAME,
			"ProcessThread created\n");
	for (;;) {
		if (mySensor->getMode() == KINECT_STEREO) {
			mySensor->depthNormalize();
			mySensor->depthInpaint();
			mySensor->createRightView();
			//mySensor->faceDetect();
			pthread_mutex_lock(&frameLocker);
			mySensor->colorMap.copyTo(mySensor->left_color);
			pthread_mutex_unlock(&frameLocker);
		} else if (mySensor->getMode() == KINECT_NOSTEREO) {
			mySensor->colorMap.copyTo(mySensor->left_color);
			mySensor->colorMap.copyTo(mySensor->right_color);
		} else if (mySensor->getMode() == KINECT_THRESHOLD) {
			mySensor->adaptiveThresholding();
		} else if (mySensor->getMode() == KINECT_OFF) {
			break;
		}
		//mySensor->keyFrameLocalize();
		//mySensor->localize();
	}
	__android_log_print(ANDROID_LOG_VERBOSE, APPNAME,
			"ProcessThread deleted\n");
	pthread_exit(NULL);
}

void *KinectPreview(void* arg) {
	JNIEnv* env;
	__android_log_print(ANDROID_LOG_DEBUG, APPNAME, "KinectThread Created\n");
	jvm->AttachCurrentThread(&env, NULL);
	VideoFrameRef depth_frame, color_frame;
	DepthSensor* mySensor = (DepthSensor*) arg;
	DepthPixel* pDepth;
	RGB888Pixel* pColor;

	if (!isKinectRunning) {
		isKinectRunning = true;
	}

	while (mySensor->getMode() != KINECT_OFF) {
		int changedStreamDummy;
		Status rc = OpenNI::waitForAnyStream(mySensor->m_streams, 2,
				&changedStreamDummy,
				SAMPLE_READ_WAIT_TIMEOUT);
		if (rc != STATUS_OK) {
			__android_log_print(ANDROID_LOG_VERBOSE, APPNAME,
					"Wait failed! (timeout is %d ms)\n%s\n",
					SAMPLE_READ_WAIT_TIMEOUT, OpenNI::getExtendedError());
			continue;
		}

		//__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "changedStreamDummy: %d", changedStreamDummy);
		Mat tmp;
		switch (changedStreamDummy) {
		case 0:
			mySensor->depth.readFrame(&depth_frame);
			if (mySensor->depthMap.empty()) {
				mySensor->depthMap.create(mySensor->getHeight(),
						mySensor->getWidth(),
						CV_16U);
				mySensor->depthMap_8b.create(mySensor->getHeight(),
						mySensor->getWidth(),
						CV_8UC1);
				mySensor->depthMap_keyframe.create(mySensor->getHeight(),
						mySensor->getWidth(),
						CV_16U);
			}
			pDepth = (DepthPixel*) depth_frame.getData();
			memcpy(mySensor->depthMap.data, pDepth,
					depth_frame.getHeight() * depth_frame.getWidth()
							* sizeof(uint16_t));
			break;
		case 1:
			mySensor->color.readFrame(&color_frame);
			if (mySensor->colorMap.empty()) {
				pthread_mutex_lock(&frameLocker);
				mySensor->colorMap.create(mySensor->getHeight(),
						mySensor->getWidth(),
						CV_8UC3);
				mySensor->left_color.create(mySensor->getHeight(),
						mySensor->getWidth(),
						CV_8UC3);
				mySensor->right_color.create(mySensor->getHeight(),
						mySensor->getWidth(),
						CV_8UC3);
				mySensor->keyframe.create(mySensor->getHeight(),
						mySensor->getWidth(), CV_8UC3);
				pthread_mutex_unlock(&frameLocker);
			}
			pColor = (RGB888Pixel*) color_frame.getData();
			memcpy(mySensor->colorMap.data, pColor,
					color_frame.getHeight() * color_frame.getWidth()
							* sizeof(RGB888Pixel));
			pthread_mutex_lock(&frameLocker);
			mySensor->left_color.copyTo(rgbImg);
			mySensor->right_color.copyTo(depthImg);
			pthread_mutex_unlock(&frameLocker);

			break;
		default:
			__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "Error in wait");
		}

	}
	__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "KinectThread deleted");
	pthread_exit(NULL);
	delete mySensor;
	return 0;
}

JNIEXPORT jstring JNICALL Java_org_gearvrf_sample_vropencv_StereoViewManager_startNativeKinect(
		JNIEnv *env, jobject thiz, jlong rgb, jlong depth) {
	env->GetJavaVM(&jvm);
	activity = env->NewGlobalRef(thiz);
	depthImg = *(Mat *) depth;
	rgbImg = *(Mat *) rgb;
	pthread_t kinect_thread, process_thread;
	pthread_mutex_init(&frameLocker, NULL);
	if (mDepthSensor == NULL) {
		mDepthSensor = new DepthSensor();
	}
	mDepthSensor->setMode(1);
	if (!isKinectRunning) {
		int rc = pthread_create(&kinect_thread, NULL, KinectPreview,
				(void*) mDepthSensor);
		if (rc) {
			__android_log_print(ANDROID_LOG_VERBOSE, APPNAME,
					"Unable to create kinect_thread");
		}

		rc = pthread_create(&process_thread, NULL, processImage,
				(void*) mDepthSensor);
		if (rc) {
			__android_log_print(ANDROID_LOG_VERBOSE, APPNAME,
					"Unable to create process_thread");
		}
		__android_log_print(ANDROID_LOG_VERBOSE, APPNAME,
				"Jai startVideo done");
		return env->NewStringUTF("Preview Started..");
	}
	//char buf[64];
	//sprintf(buf, "status = %d\n", status);
	return env->NewStringUTF("Kinect already running");
}

JNIEXPORT jint JNICALL Java_org_gearvrf_sample_vropencv_StereoViewManager_setNativeStereoMode(
		JNIEnv* env, jobject thiz, jint mode) {
	jint status = 0;
	__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "mode = %d\n", mode);
	if (isKinectRunning) {
		mDepthSensor->setMode(mode);
	} else {
		return -1;
	}
	if (mode == KINECT_OFF && mDepthSensor != NULL) {
		delete mDepthSensor;
		mDepthSensor = NULL;
	}

	return status;
}

JNIEXPORT jfloatArray JNICALL Java_org_gearvrf_sample_vropencv_StereoViewManager_setNativeProcessingMode(
		JNIEnv * env, jobject thiz, jint processmode) {
	jfloatArray result = env->NewFloatArray(16);
	jfloat *tmp = env->GetFloatArrayElements(result, NULL);

	__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "processmode = %d\n",
			processmode);
	if (isKinectRunning) {
		mDepthSensor->setKeyframe(true);
		Mat T = mDepthSensor->extractSurfaceNormalCenter();
		for (int i = 0; i < 16; i++) {
			tmp[i] = T.at<float>(i / 4, i % 4);
		}
	}
	env->ReleaseFloatArrayElements(result, tmp, NULL);
	//env->SetFloatArrayRegion(result, 0, 16, tmp);
	__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "Returning result\n");
	return result;
}

JNIEXPORT jint JNICALL Java_org_gearvrf_sample_vropencv_StereoViewManager_getNativeUSBRootPermission(
		JNIEnv *, jobject) {
	__android_log_print(ANDROID_LOG_VERBOSE, APPNAME,
			"Acquiring USB permission for Sensor\n");
	int r = system("su -c \"chmod -R 777 /dev/bus/usb/\"");
	if (r != 0) {
		__android_log_print(ANDROID_LOG_VERBOSE, APPNAME,
				"Could not grant permissions to USB\n");
	}
	return r;
}
