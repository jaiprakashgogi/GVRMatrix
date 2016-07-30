package com.android.texample2;

import android.util.Log;

public class Character {
	
	int timer;
	int x, y;
	private GLText glText;
	private char c;
	
	public Character(GLText _glText, char _c, int speed, int x2, int y2) {
		// TODO Auto-generated constructor stub
		x = x2;
		y = y2;
		timer = speed;
		glText = _glText;
		c = _c;
		Log.i("Character", "Char is: " + c + ":" + x + "x" + y);
	}

	public void update() {
		// TODO Auto-generated method stub
		glText.draw( c, x, y);
	}
}
