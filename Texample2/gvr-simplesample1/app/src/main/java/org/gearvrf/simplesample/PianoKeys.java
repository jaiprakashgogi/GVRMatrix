package org.gearvrf.simplesample;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.Log;

import java.util.ArrayList;

public class PianoKeys {
	private SoundPool soundpoolObject;
	private ArrayList<Boolean> loadBooleanList;
	private ArrayList<Integer> soundIdList;
	private Context androidContext;
	public PianoKeys(Context context) {
		// TODO Auto-generated constructor stub
		androidContext = context;
		initParams();
		soundPoolLoadFunc();
	}

	private void initParams() {
		// TODO Auto-generated method stub
		loadBooleanList = new ArrayList<Boolean>();		
		for( int i = 0; i < 8; ++i)
		{
			boolean tmp = false;
			Log.d("shiyu", " "+i);
			loadBooleanList.add(tmp);
		}
	}

	private void soundPoolLoadFunc() {
		// TODO Auto-generated method stub
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
						case 2:
							loadBooleanList.set(1, true);
							break;
						case 3:
							loadBooleanList.set(2, true);
							break;
						case 4:
							loadBooleanList.set(3, true);
							break;
						case 5:
							loadBooleanList.set(4, true);
							break;
						case 6:
							loadBooleanList.set(5, true);
							break;
						case 7:
							loadBooleanList.set(6, true);
							break;
						case 8:
							loadBooleanList.set(7, true);
							break;
					}
				}
				
			}
		});
		soundIdList = new ArrayList<Integer>();
		soundIdList.add(soundpoolObject.load(androidContext, R.raw.a1, 1));
		soundIdList.add(soundpoolObject.load(androidContext, R.raw.a2, 1));
		soundIdList.add(soundpoolObject.load(androidContext, R.raw.a3, 1));
		soundIdList.add(soundpoolObject.load(androidContext, R.raw.a4, 1));
		soundIdList.add(soundpoolObject.load(androidContext, R.raw.a5, 1));
		soundIdList.add(soundpoolObject.load(androidContext, R.raw.a6, 1));
		soundIdList.add(soundpoolObject.load(androidContext, R.raw.a7, 1));
		soundIdList.add(soundpoolObject.load(androidContext, R.raw.a8, 1));
		
		
	}
	
	public void playMusic(int id)
	{
		int resId = 0;
		Log.e("Shiyu", "id: " + id);
		if(loadBooleanList.get(id))
		{
			Log.d("shiyu", "successful");
			resId = soundIdList.get(id);
		}
		soundpoolObject.play(resId, 1.5f, 1.5f, 1, 0, 1.0f);
	}
	
	
}
