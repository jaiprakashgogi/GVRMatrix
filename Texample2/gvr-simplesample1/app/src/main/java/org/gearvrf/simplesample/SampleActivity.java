/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.simplesample;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;


public class SampleActivity extends GVRActivity {

    SampleMain mSampleMain;
    private long lastDownTime = 0;
	private MediaPlayer player;
	private MediaPlayer player0;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mSampleMain = new SampleMain();
        setScript(mSampleMain, "gvr.xml");
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            lastDownTime = event.getDownTime();
        }

        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            // check if it was a quick tap
            if (event.getEventTime() - lastDownTime < 200) {
                // pass it as a tap to the Main
                mSampleMain.onTap();
            }
        }

        return true;
    }
    
	public void onResume() {
		Log.d("shiyu", "In onResume");
		super.onResume();
		player = MediaPlayer.create(this, R.raw.spacesynth);
		//player.prepareAsync();
		player.setLooping(true);
		player.setVolume(1.0f, 1.0f);
		player.start();
		player0 = MediaPlayer.create(this, R.raw.dobroide);
		//player0.prepareAsync();
		player0.setLooping(true);
		player0.setVolume(1.0f, 1.0f);
		player0.start();
	}
	
	public void onStop()
	{
		if(player.isLooping())
		{
			player.stop();
			player.release();
		}
		if(player0.isLooping())
		{
			player0.stop();
			player0.release();
		}
		//soundpoolObject.release();
		super.onStop();
	}
}
