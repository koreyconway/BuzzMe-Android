package org.sgnexus.buzzme;

import org.sgnexus.buzzme.ShakeDetector.OnShakeListener;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;

public class MainActivity extends Activity implements OnShakeListener {
	private final String tag = this.getClass().getName();
	private Vibrator mVibrator;
	private ShakeDetector mShakeDetector;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private EditText mVName;
	private Firebase mFb;
	private String mDeviceId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		mShakeDetector = new ShakeDetector(this);
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mVName = (EditText) this.findViewById(R.id.name);
		mDeviceId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(mShakeDetector, mAccelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
		mFb = new Firebase("https://buzz-me.firebaseio.com/");
		mFb.addChildEventListener(new ChildEventListener() {
			@Override
			public void onCancelled() {
				// Ignore
			}

			@Override
			public void onChildAdded(DataSnapshot snap, String previousChildName) {
				if (!snap.getValue().equals(mDeviceId)) {
					vibrate();
					Log.d(tag, "received friend's snapshot: " + snap.getName()
							+ snap.getValue());
				} else {
					Log.d(tag, "received my own snapshot: " + snap.getValue());
				}
			}

			@Override
			public void onChildChanged(DataSnapshot snap,
					String previousChildName) {
				// Ignore
			}

			@Override
			public void onChildMoved(DataSnapshot snap, String previousChildName) {
				// Ignore
			}

			@Override
			public void onChildRemoved(DataSnapshot arg0) {
				// Ignore
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(mShakeDetector);
		mFb = null;
	}

	private void vibrate() {
		AsyncTask<Void, Void, Void> at = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				mVibrator.vibrate(300); // Vibrate for 300 milliseconds
				Log.d(tag, "vibrating");
				return null;
			}
		};
		at.execute();
	}

	@Override
	public void onShake() {
		Toast.makeText(getApplicationContext(), "Sending shake",
				Toast.LENGTH_LONG).show();
		Firebase fbPush = mFb.push();

		if (mVName.getText().length() > 0) {
			mDeviceId = mVName.getText().toString();
		}

		fbPush.setValue(mDeviceId);
	}
}
