package org.gearvrf.simplesample;

import android.os.AsyncTask;

import org.gearvrf.utility.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends AsyncTask<Void, Void, Void> {

	String dstAddress;
	int dstPort;

	private SampleMain sample;

	Client(SampleMain mSamplemain, String addr, int port) {
		dstAddress = addr;
		dstPort = port;
		sample = mSamplemain;
	}

	@Override
	protected Void doInBackground(Void... arg0) {

		Socket socket = null;

		try {
			socket = new Socket(dstAddress, dstPort);

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(
					10);
			byte[] buffer = new byte[10];

			int bytesRead;
			InputStream inputStream = socket.getInputStream();

			/*
			 * notice: inputStream.read() will block if no data return
			 */
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				//byteArrayOutputStream.write(buffer, 0, bytesRead);
				String response = "";
				response = byteArrayOutputStream.toString("UTF-8");

				int val = (int)((char) response.charAt(0) - 48);
				Log.e("Shiyu", "msg: " + response + " val: " + val);
				Log.e("Shiyu", "val: " + val);
				sample.execute(val);
			}

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//response = "UnknownHostException: " + e.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//response = "IOException: " + e.toString();
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
	}

}
