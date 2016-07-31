package org.gearvrf.simplesample;

import java.util.ArrayList;

import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.Log;
import android.app.Activity;

public class PianoKeys {
	private SoundPool soundpoolObject;
	private ArrayList<Boolean> loadBooleanList;
	
	public PianoKeys() {
		// TODO Auto-generated constructor stub

		initParams();
		soundPoolLoadFunc();
	}

	private void initParams() {
		// TODO Auto-generated method stub
	/*	loadBooleanList = new ArrayList<Boolean>();		
		for( int i = 0; i < 7; ++i)
		{
			boolean tmp = false;
			Log.d("shiyu", " "+i);
			loadBooleanList.add(tmp);
		}*/
	}

	private void soundPoolLoadFunc() {
/*		// TODO Auto-generated method stub
		soundpoolObject = new SoundPool(12, AudioManager.STREAM_MUSIC, 0);
		soundpoolObject.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			

			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) 
			{
				// TODO Auto-generated method stub
				if(status == 0)
				{
					switch(sampleId)
					{
						case 1:
							loadBooleanList.set(0, true);
							break;
					}
				}
				
			}
		});
		soundIdList = new ArrayList<Integer>();*/
	}
	
	
}
