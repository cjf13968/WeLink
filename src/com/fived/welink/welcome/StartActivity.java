package com.fived.welink.welcome;

import com.fived.welink.R;
import com.fived.welink.dataproc.DataStoreManager;
import com.fived.welink.wifidirect.WiFiDirectActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObservable;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * ∆Ù∂ØΩÁ√Ê
 */
public class StartActivity extends Activity {
	private ImageView imageView = null;
	private TextView textView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_activity);

		imageView = (ImageView) findViewById(R.id.start_image);
		textView = (TextView) findViewById(R.id.start_text);
		Animation animation1 = AnimationUtils.loadAnimation(StartActivity.this,
				R.anim.mov);
		imageView.startAnimation(animation1);
		Animation animation = AnimationUtils.loadAnimation(StartActivity.this,
				R.anim.alpha);
		textView.startAnimation(animation);
	}

	@Override
	protected void onStart() {
		WifiManager wifiManager = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(true);
		new MyAsynTask().execute();
		super.onStart();
	}

	class MyAsynTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			publishProgress();
			try {
				Thread.sleep(2500);
			} catch (Exception e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			boolean isFirstStart = DataStoreManager.getDefaultInstance(
					StartActivity.this).getStartStauts();
			Intent it = null;
			if (isFirstStart) {
				it = new Intent(StartActivity.this, HelpActivity.class);
			} else {
				it = new Intent(StartActivity.this, WiFiDirectActivity.class);
			}
			startActivity(it);
			overridePendingTransition(R.anim.hold, R.anim.hold);
			StartActivity.this.finish();
			super.onPostExecute(result);
		}

	}
}
