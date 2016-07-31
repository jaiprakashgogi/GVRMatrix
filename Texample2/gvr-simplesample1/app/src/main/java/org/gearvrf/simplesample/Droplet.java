package org.gearvrf.simplesample;

import org.gearvrf.GVRContext;
import org.gearvrf.utility.Log;

import java.util.ArrayList;
import java.util.Random;

public class Droplet {

	float x, y;
	int speed, curr_speed = 0;
	int length;
	float step;
	ArrayList<Character> mCharacter;
	private Random rand;
	private int MAX_DROPLET_SIZE = 25;
	private int MAX_DROPLET_SPEED = 10;
	private DropletCbListener cb;
	private float MAX_Y;
	private boolean firstDelete = false;
	float CHAR_HEIGHT = 0.1f;
	float CHAR_PAD = 0.02f;
	int MAX_RES = 10;
	private GVRContext gvrContext;

	Droplet(GVRContext _gvrContext, float position) {
		gvrContext = _gvrContext;
		rand = new Random();
		x = position;
		y = 2.5f;
		speed = rand.nextInt(MAX_DROPLET_SPEED) + 1;
		length = rand.nextInt(MAX_DROPLET_SIZE) + 1;
		step = CHAR_HEIGHT + 2 * CHAR_PAD;
		MAX_Y = -2.5f;
		mCharacter = new ArrayList<Character>();

		char c = (char) (rand.nextInt(MAX_RES) + 1);
		mCharacter.add(new Character(gvrContext, c, x, y));
	}
	
	public void setDropletCbListener(DropletCbListener _cb) {
		cb = _cb;
	}

	void update() {
		Log.i("Droplet", "curr_speed: " + curr_speed + " Speed: " + speed + " Len: " + length);
		Log.i("Droplet", "x: " + x + " x " + y);

		if (curr_speed++ > speed) {
			if (mCharacter.size() > length) {
				mCharacter.get(0).remove();
				mCharacter.remove(mCharacter.get(0));
				if(!firstDelete) {
					firstDelete = true;
				}
			}
			Log.i("Droplet" , "Jai is here");
			char c = (char) (rand.nextInt(MAX_RES) + 1);
			y -= step;
			mCharacter.add(new Character(gvrContext, c, x, y));
			curr_speed = 0;
			if( y<= MAX_Y - length * 0.1f) {
				cb.onDropletDone(this);
			}
		}

		for (int i = 0; i < mCharacter.size(); i++) {
			mCharacter.get(i).update();
		}

	}

	void remove() {
		for (int i = 0; i < mCharacter.size(); i++) {
			mCharacter.get(i).remove();
		}
	}


}
