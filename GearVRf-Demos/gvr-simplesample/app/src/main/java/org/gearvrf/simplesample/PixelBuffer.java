package org.gearvrf.simplesample;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.IntBuffer;

/**
 * Created by jaiprakashgogi on 7/30/16.
 */

public class PixelBuffer {
    final static String TAG = "PixelBuffer";
    final static boolean LIST_CONFIGS = false;

    GLSurfaceView.Renderer mRenderer; // borrow this interface
    int mWidth, mHeight;
    Bitmap mBitmap;

    String mThreadOwner;

    public PixelBuffer(int width, int height) {
        mWidth = width;
        mHeight = height;
        // Record thread owner of OpenGL context
        mThreadOwner = Thread.currentThread().getName();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Bitmap bm = getBitmap();
                }
            }
        }).start();
    }

    public void setRenderer(GLSurfaceView.Renderer renderer) {
        mRenderer = renderer;

        // Does this thread own the OpenGL context?
        if (!Thread.currentThread().getName().equals(mThreadOwner)) {
            Log.e(TAG, "setRenderer: This thread does not own the OpenGL context.");
            return;
        }

        // Call the renderer initialization routines
        mRenderer.onSurfaceCreated(null, null);
        mRenderer.onSurfaceChanged(null, mWidth, mHeight);
    }

    public Bitmap getBitmap() {
        // Do we have a renderer?
        if (mRenderer == null) {
            Log.e(TAG, "getBitmap: Renderer was not set.");
            return null;
        }

        // Does this thread own the OpenGL context?
        if (!Thread.currentThread().getName().equals(mThreadOwner)) {
            Log.e(TAG, "getBitmap: This thread does not own the OpenGL context.");
            return null;
        }

        // Call the renderer draw routine
        mRenderer.onDrawFrame(null);
        convertToBitmap();
        return mBitmap;
    }


    private void convertToBitmap() {
//        IntBuffer ib = IntBuffer.allocate(mWidth*mHeight);
//        IntBuffer ibt = IntBuffer.allocate(mWidth*mHeight);
//        mGL.glReadPixels(0, 0, mWidth, mHeight, GL_RGBA, GL_UNSIGNED_BYTE, ib);
//
//        // Convert upside down mirror-reversed image to right-side up normal image.
//        for (int i = 0; i < mHeight; i++) {
//            for (int j = 0; j < mWidth; j++) {
//                ibt.put((mHeight-i-1)*mWidth + j, ib.get(i*mWidth + j));
//            }
//        }
//
        int b[] = new int[(int) mWidth*mHeight];
        int bt[] = new int[(int) mWidth*mHeight];
        IntBuffer buffer = IntBuffer.wrap(b);
        buffer.position(0);
        GLES20.glReadPixels(0, 0, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
        if(mBitmap == null) {
            mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        }
        mBitmap.copyPixelsFromBuffer(buffer);
//        mBitmap.copyPixelsFromBuffer(ibt);
    }
}