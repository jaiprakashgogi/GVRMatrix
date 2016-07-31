package org.gearvrf.simplesample;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.utility.Log;

import java.util.ArrayList;

public class Rain implements GVRDrawFrameListener{

	private float WALL_SIZE = 5.f;
	private ArrayList<Rainlet> rainlets;
	private int MAX_COL = 10;
	GVRContext gvrContext;
	
	public Rain(GVRContext _gvrContext) {
		// TODO Auto-generated constructor stub
		gvrContext = _gvrContext;
		gvrContext.registerDrawFrameListener(this);
		rainlets = new ArrayList<Rainlet>();
		for(int i=0; i< MAX_COL; i++) {
			float position = - WALL_SIZE/2 + (i-1) * (WALL_SIZE/MAX_COL);
			rainlets.add(new Rainlet(gvrContext, position));
		}
	}
	
	public void update() {
		// TODO Auto-generated method stub
		for(int i = 0; i< rainlets.size(); i++) {
			rainlets.get(i).update();
		}
	}


	@Override
	public void onDrawFrame(float frameTime) {
		Log.i("Rain", "Update");
		update();
	}
}
