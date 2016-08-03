package org.gearvrf.simplesample;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterialShaderId;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderTexture;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

/**
 * Created by jaiprakashgogi on 7/30/16.
 */
public class GVRTexSceneObject extends GVRSceneObject implements GVRDrawFrameListener {

    private GVRContext gvrContext;
    GVRTexture mTexture;
    GVRRenderTexture mRenderTexture;
    //private Surface mSurface;
    //private SurfaceTexture mSurfaceTexture;
    int width, height;
    //private PixelBuffer mPixelBuffer;
    //private Texample2Renderer mTexample2Renderer;
    //private Rain rain;

    private GLText glText;
    private Rain rain;
    int CHAR_HEIGHT = 28;
    int CHAR_PAD = 4;
    private float[] mProjMatrix = new float[16];
    private float[] mVMatrix = new float[16];
    private float[] mVPMatrix = new float[16];

    private boolean isFirstFrame = true;
    int status = 0;

    private static final String TAG = "TexampleRenderer";

    public GVRTexSceneObject(GVRContext _gvrContext, GVRMesh mesh) {
        super(_gvrContext, mesh);
        gvrContext = _gvrContext;
        gvrContext.registerDrawFrameListener(this);


//        GVRTexture texture = new GVRExternalTexture(gvrContext);
//        GVRMaterial material = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.OES.ID);
//        material.setMainTexture(texture);
//        getRenderData().setMaterial(material);
//
//        this.gvrContext = gvrContext;
//        width = height = 1024;
//        mSurfaceTexture = new SurfaceTexture(texture.getId());
//        mSurface = new Surface(mSurfaceTexture);
//        mSurfaceTexture.setDefaultBufferSize(width, height);
//        mPixelBuffer = new PixelBuffer(width, height);
//        mTexample2Renderer =  new Texample2Renderer(gvrContext.getContext());
//        mPixelBuffer.setRenderer(mTexample2Renderer);
 //       GVRTexture texture = gvrContext.loadTexture(new GVRAndroidResource(
 //               gvrContext, R.drawable.gearvr_logo));

        mRenderTexture = new GVRRenderTexture(gvrContext, 1024, 1024);
        int myrendertextureid = mRenderTexture.getId();
        Log.e("RC", "texture id is "+ myrendertextureid);
        mTexture = new GVRBitmapTexture(gvrContext, myrendertextureid);
        glText = new GLText(gvrContext.getContext().getAssets());

        glText.load( "Roboto-Regular.ttf", CHAR_HEIGHT, CHAR_PAD, CHAR_PAD );  // Create Font (Height: 14 Pixels / X+Y Padding 2 Pixels)
        rain = new Rain(this, glText);

        GVRMaterial material = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.Texture.ID);
        material.setMainTexture(mTexture);
        getRenderData().setMaterial(material);

        width = height = 1024;
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        // Take into account device orientation
        if (width > height) {
            Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 1, 10);
        }
        else {
            Matrix.frustumM(mProjMatrix, 0, -1, 1, -1/ratio, 1/ratio, 1, 10);
        }

        // Save width and height
        this.width = width;                             // Save Current Width
        this.height = height;                           // Save Current Height

        int useForOrtho = Math.min(width, height);

        //TODO: Is this wrong?
        Matrix.orthoM(mVMatrix, 0,
                -useForOrtho/2,
                useForOrtho/2,
                -useForOrtho/2,
                useForOrtho/2, 0.1f, 100f);
    }

    public GVRTexSceneObject(GVRContext gvrContext, float _width, float _height) {
        this(gvrContext, gvrContext.createQuad(_width, _height));
    }


    @Override
    public void onDrawFrame(float frameTime) {
//        Bitmap frame = mPixelBuffer.getBitmap();
//        Canvas canvas = mSurface.lockCanvas(null);
//
//        canvas.drawBitmap(frame, new Rect(0, 0, frame.getWidth(), frame.getHeight()), new Rect((canvas.getWidth() - frame.getWidth())/ 2,
//                (canvas.getHeight() - frame.getHeight())/ 2,
//                (canvas.getWidth() - frame.getWidth())/ 2 + frame.getWidth(),
//                (canvas.getHeight() - frame.getHeight())/ 2 + frame.getHeight()), null);
//        mSurface.unlockCanvasAndPost(canvas);
//           mSurfaceTexture.updateTexImage();

        //GLES20.glBindBuffer();
 /*       int []old = new int[2];

        //GLES20.glGenBuffers(2, old, 0);
        //GLES20.glBindBuffer(GLES20.GL_FRAMEBUFFER_BINDING, mVBOid[0]);
     //   GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, old, 0);
 //       GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, old, 0);
        if(isFirstFrame) {
            mRenderTexture = new GVRRenderTexture(gvrContext, 1024, 1024);
            int myrendertextureid = mRenderTexture.getId();
            Log.e("RC", "texture id is "+ myrendertextureid);
            mTexture = new GVRBitmapTexture(gvrContext, myrendertextureid);
            //getRenderData().getMaterial().setMainTexture(mTexture);

            //GLES20.glClearColor( 0.f, 0.f, 0.f, 0.5f );

            // Create the GLText
            glText = new GLText(gvrContext.getContext().getAssets());

            // Load the font from file (set size + padding), creates the texture
            // NOTE: after a successful call to this the font is ready for rendering!
            glText.load( "Roboto-Regular.ttf", CHAR_HEIGHT, CHAR_PAD, CHAR_PAD );  // Create Font (Height: 14 Pixels / X+Y Padding 2 Pixels)

            // enable texture + alpha blending
            //GLES20.glEnable(GLES20.GL_BLEND);
            //GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            rain = new Rain(this, glText);
            isFirstFrame = false;
        } else {

            mRenderTexture.bind();

           int clearMask = GLES20.GL_COLOR_BUFFER_BIT;
            GLES20.glClear(clearMask);
            Matrix.multiplyMM(mVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
            // TEST: render the entire font texture
            //glText.drawTexture( width/2, height/2, mVPMatrix);            // Draw the Entire Texture
           glText.begin( 0.0f, 1.0f, 0.0f, 1.0f, mVPMatrix );         // Begin Text Rendering (Set Color BLUE)
            rain.update();
            glText.end();                                   // End Text Rendering
 }*/

   //     GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, old[0]);
        int []old = new int[2];
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, old, 0);
        mRenderTexture.bind();

        GLES20.glClearColor(0,0,0,1);
        status++;
        if(status > 99) {
            status = 0;
        }

        int clearMask = GLES20.GL_COLOR_BUFFER_BIT;
        GLES20.glClear(clearMask);
        Matrix.multiplyMM(mVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
        if(GLES20.glGetError()!=GLES20.GL_NO_ERROR)
            Log.e("RC","Jai is dead 1");
        else
            Log.e("RC","jai wont die 1");

        glText.drawTexture( width/2, height/2, mVPMatrix);            // Draw the Entire Texture

        glText.begin( 0.0f, 1.0f, 0.0f, 1.0f, mVPMatrix );         // Begin Text Rendering (Set Color BLUE)
        rain.update();
        glText.end();                                   // End Text Rendering

        if(GLES20.glGetError()!=GLES20.GL_NO_ERROR)
            Log.e("RC","Jai is dead");
        else
        Log.e("RC","jai wont die");
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, old[0]);
    }
}
