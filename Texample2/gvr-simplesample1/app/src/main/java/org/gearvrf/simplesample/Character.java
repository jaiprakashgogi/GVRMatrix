package org.gearvrf.simplesample;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

import java.io.IOException;
import java.util.Random;

public class Character {
	float x, y ,z;
	private char c;
	GVRSceneObject sceneObject;
	int MAX_DEPTH = 5;
	GVRScene scene;
	
	public Character(GVRContext gvrContext, char _c, float x2, float y2, float z2) {
		// TODO Auto-generated constructor stub
		scene = gvrContext.getMainScene();
		x = x2;
		y = y2;
		z = z2;
		//glText = _glText;
		c = _c;
		Random rand = new Random();
		z = -5 - rand.nextInt(MAX_DEPTH) + 1;
		//length = rand.nextInt(MAX_DROPLET_SIZE) + 1;
		//GVRTexture texture;
		try {
			GVRTexture texture = gvrContext.loadTexture(new GVRAndroidResource(gvrContext.getContext(), "hello (" + new Random().nextInt(360) + ").png"));
			sceneObject = new GVRSceneObject(gvrContext, 0.2f, 0.2f, texture);
			sceneObject.getTransform().setPosition(x, y, z);
			gvrContext.getMainScene().addSceneObject(sceneObject);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Log.i("Character", "Creating character: " + x + "x" + y);
	}

	public void update() {
		// TODO Auto-generated method stub
		//glText.draw( c, x, y);
	}

	public void remove() {
		if(sceneObject != null)
		sceneObject.getParent().removeChildObject(sceneObject);

	}
}
