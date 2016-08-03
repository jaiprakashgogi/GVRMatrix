package org.gearvrf.simplesample;

import java.util.ArrayList;
import java.util.Random;

public class Rainlet implements DropletCbListener{
	// Rain in the manager of the droplets
	ArrayList<Droplet> mDroplets;
	private int delay, curr_delay = 0;
	private GVRTexSceneObject texample2Renderer;
	private GLText glText;
	private int MAX_DELAY = 50;
	private Random rand;
	private boolean isDelayStart = false;
	private int position;

	public Rainlet(GVRTexSceneObject _texample2Renderer, GLText _glText, int _position) {
		// TODO Auto-generated constructor stub
		position = _position;
		texample2Renderer = _texample2Renderer;
		glText = _glText;
		mDroplets = new ArrayList<Droplet>();
		rand = new Random();
		delay = rand.nextInt(MAX_DELAY) + 1;
		Droplet mDrop = new Droplet(texample2Renderer, glText, position);
		mDrop.setDropletCbListener(this);
		mDroplets.add(mDrop);
	}

	public void update() {
		// TODO Auto-generated method stub
		if(curr_delay++ > delay && isDelayStart){
			Droplet mDrop = new Droplet(texample2Renderer, glText, position);
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
		mDroplets.remove(drop);
		isDelayStart = true;
	}

	@Override
	public void onTailDelete() {
		// TODO Auto-generated method stub
		isDelayStart = true;
	}
	
	

}
