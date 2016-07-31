package org.gearvrf.simplesample;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

public class Character {
	float x, y;
	private char c;
	GVRSceneObject sceneObject;
	
	public Character(GVRContext gvrContext, char _c, float x2, float y2) {
		// TODO Auto-generated constructor stub
		x = x2;
		y = y2;
		//glText = _glText;
		c = _c;
		GVRTexture texture = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.gearvr_logo));
		sceneObject = new GVRSceneObject(gvrContext, 0.1f, 0.1f, texture);
		sceneObject.getTransform().setPosition(x, y, -5.f);
		gvrContext.getMainScene().addSceneObject(sceneObject);
		//Log.i("Character", "Creating character: " + x + "x" + y);
	}

	public void update() {
		// TODO Auto-generated method stub
		//glText.draw( c, x, y);
	}

	public void remove() {
		sceneObject.getParent().removeChildObject(sceneObject);
	}
}
