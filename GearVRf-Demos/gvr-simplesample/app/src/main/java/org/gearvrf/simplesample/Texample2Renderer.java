package org.gearvrf.simplesample;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Texample2Renderer implements GLSurfaceView.Renderer  {

	private static final String TAG = "TexampleRenderer";
	GLText glText;                             // A GLText Instance
	private Context context;                           // Context (from Activity)

	int width = 720;                           // Updated to the Current Width + Height in onSurfaceChanged()
	int height = 1280;
	private float[] mProjMatrix = new float[16];
	private float[] mVMatrix = new float[16];
	private float[] mVPMatrix = new float[16];
	private float pos = 0.f;
	Rain rain;
	int CHAR_HEIGHT = 28;
	int CHAR_PAD = 4;

	public Texample2Renderer(Context context)  {
		super();
		this.context = context;                         // Save Specified Context
	}

	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		// Set the background frame color
		GLES20.glClearColor( 0.f, 0.f, 0.f, 0.5f );
		
		// Create the GLText
		glText = new GLText(context.getAssets());

		// Load the font from file (set size + padding), creates the texture
		// NOTE: after a successful call to this the font is ready for rendering!
		glText.load( "Roboto-Regular.ttf", CHAR_HEIGHT, CHAR_PAD, CHAR_PAD );  // Create Font (Height: 14 Pixels / X+Y Padding 2 Pixels)

		// enable texture + alpha blending
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		//rain = new Rain(this, glText);
	}

	public void onDrawFrame(GL10 unused) {
		// Redraw background color
		int clearMask = GLES20.GL_COLOR_BUFFER_BIT;

		GLES20.glClear(clearMask);
	
		Matrix.multiplyMM(mVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
		
		
		// TEST: render the entire font texture
		//glText.drawTexture( width/2, height/2, mVPMatrix);            // Draw the Entire Texture
		glText.begin( 0.0f, 1.0f, 0.0f, 1.0f, mVPMatrix );         // Begin Text Rendering (Set Color BLUE)
		rain.update();
		glText.end();                                   // End Text Rendering
	}

	public void onSurfaceChanged(GL10 unused, int width, int height) { //		gl.glViewport( 0, 0, width, height ); 
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
}
