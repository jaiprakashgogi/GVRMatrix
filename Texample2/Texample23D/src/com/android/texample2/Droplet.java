package com.android.texample2;

import java.util.ArrayList;
import java.util.Random;

public class Droplet {

	int x, y;
	int speed, curr_speed = 0;
	int length;
	int step;
	ArrayList<Character> mCharacter;
	private Random rand;
	private GLText glText;
	private int MAX_DROPLET_SIZE = 25;
	private int MAX_DROPLET_SPEED = 10;
	private DropletCbListener cb;
	private int MAX_Y;
	private boolean firstDelete = false;

	Droplet(Texample2Renderer texample2Renderer, GLText _glText, int position) {
		glText = _glText;
		rand = new Random();
		x = position;
		y = 0;
		speed = rand.nextInt(MAX_DROPLET_SPEED) + 1;
		length = rand.nextInt(MAX_DROPLET_SIZE) + 1;
		step = texample2Renderer.CHAR_HEIGHT + 2 * texample2Renderer.CHAR_PAD;
		MAX_Y = -1280;
		mCharacter = new ArrayList<Character>();

		char c = (char) (glText.CHAR_START + rand.nextInt(glText.CHAR_CNT) + 1);
		mCharacter.add(new Character(glText, c, x, y));
	}
	
	public void setDropletCbListener(DropletCbListener _cb) {
		cb = _cb;
	}

	void update() {
		if (curr_speed++ > speed) {
			if (mCharacter.size() > length) {
				mCharacter.remove(mCharacter.get(0));
				if(!firstDelete) {
					//cb.onTailDelete();
					firstDelete = true;
				}
			}
			char c = (char) (glText.CHAR_START + rand.nextInt(glText.CHAR_CNT) + 1);
			y -= step;
			mCharacter.add(new Character(glText, c, x, y));
			curr_speed = 0;
			if( y<= MAX_Y) {
				cb.onDropletDone(this);
			}
		}

		for (int i = 0; i < mCharacter.size(); i++) {
			mCharacter.get(i).update();
		}

	}

}
