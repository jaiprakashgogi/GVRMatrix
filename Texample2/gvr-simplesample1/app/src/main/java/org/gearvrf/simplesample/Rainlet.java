package org.gearvrf.simplesample;

import org.gearvrf.GVRContext;

import java.util.ArrayList;
import java.util.Random;

public class Rainlet implements DropletCbListener{
	// Rain in the manager of the droplets
	ArrayList<Droplet> mDroplets;
	private int delay, curr_delay = 0;
	private int MAX_DELAY = 50;
	private Random rand;
	private boolean isDelayStart = false;
	private float position, z;
	GVRContext gvrContext;

	public Rainlet(GVRContext _gvrContext, float _y, float _z) {
		// TODO Auto-generated constructor stub
		gvrContext = _gvrContext;
		position = _y;
		z = _z;
		mDroplets = new ArrayList<Droplet>();
		rand = new Random();
		delay = rand.nextInt(MAX_DELAY) + 1;
		Droplet mDrop = new Droplet(gvrContext, position, z);
		mDrop.setDropletCbListener(this);
		mDroplets.add(mDrop);
	}

	public void update() {
		// TODO Auto-generated method stub
		if(curr_delay++ > delay && isDelayStart){
			Droplet mDrop = new Droplet(gvrContext, position, z);
			mDrop.setDropletCbListener(this);
			mDroplets.add(mDrop);
			isDelayStart = false;
			curr_delay = 0;
		}
		for(int i=0; i< mDroplets.size(); i++) {
			mDroplets.get(i).update();
		}
	}
	

	@Override
	public void onDropletDone(Droplet drop) {
		// TODO Auto-generated method stub
		drop.remove();
		mDroplets.remove(drop);
		isDelayStart = true;
	}


}
