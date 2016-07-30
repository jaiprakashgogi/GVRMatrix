package com.android.texample2;

import java.util.ArrayList;
import java.util.Random;

public class Droplet {

	int x, y;
	int speed;
	int length;
	int step;
	ArrayList<Character> mCharacter;
	private Random rand;
	private GLText glText;

	Droplet(Texample2Renderer texample2Renderer, GLText _glText) {
		glText = _glText;
		x = 0;
		y = 0;
		speed = 1;
		length = 100;
		step = texample2Renderer.CHAR_HEIGHT + 2*texample2Renderer.CHAR_PAD;
		mCharacter = new ArrayList<Character>();
		rand = new Random();
		char c = (char) (glText.CHAR_START + rand.nextInt(glText.CHAR_CNT) + 1);
		mCharacter.add(new Character(glText, c, speed, x, y));
	}

	void update() {
		if (mCharacter.size() > length) {
			mCharacter.remove(mCharacter.get(0));
		}
		char c = (char) (glText.CHAR_START + rand.nextInt(glText.CHAR_CNT) + 1);
		y += step;
		mCharacter.add(new Character(glText, c, speed, x, y));
		
		for(int i =0; i < mCharacter.size(); i ++) {
			mCharacter.get(i).update();
		}

	}

}
