package org.gearvrf.simplesample;

import java.util.ArrayList;

public class Rain {
	
	private GVRTexSceneObject texample2Renderer;
	private GLText glText;
	private ArrayList<Rainlet> rainlets;
	private int MAX_COL = 10;
	
	public Rain(GVRTexSceneObject texample2Renderer2, GLText glText2) {
		// TODO Auto-generated constructor stub
		texample2Renderer = texample2Renderer2;
		glText = glText2;
		rainlets = new ArrayList<Rainlet>();
		for(int i=0; i< MAX_COL; i++) {
			int position = -texample2Renderer.width/2 + (i-1) * (texample2Renderer.width/MAX_COL);
			rainlets.add(new Rainlet(texample2Renderer, glText, position));
		}
	}
	
	public void update() {
		// TODO Auto-generated method stub
		for(int i = 0; i< rainlets.size(); i++) {
			rainlets.get(i).update();
		}
	}
	

}
