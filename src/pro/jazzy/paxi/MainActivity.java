package pro.jazzy.paxi;

import pro.jazzy.paxi.GPSTrackingService.LocalBinder;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String TAG = "Paxi";

	static final int PICK_CONTACT_REQUEST = 0;

	private static final String REFRESH_DATA_INTENT = "jazzy_gps_refresh";

	GPSTrackingService trackingService;

	TrackingUpdateReceiver updateReceiver;

	boolean trackingBounded = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Button btnAdd = (Button) findViewById(R.id.btnAdd);
		Button btnGpsOn = (Button) findViewById(R.id.btnGpsOn);
		Button btnGpsOff = (Button) findViewById(R.id.btnGpsOff);

		btnAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						ContactsActivity.class);
				intent.putExtra("membersCount", 4);
				startActivityForResult(intent, PICK_CONTACT_REQUEST);
			}
		});
		btnGpsOn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String msg;
				if (!trackingService.isTracking()) {
					trackingService.start();
					msg = "Tracking is on...";
				} else {
					msg = "Tracking already started!";
				}
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT)
						.show();
			}
		});
		btnGpsOff.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String msg = "Tracking is off...";
				if (trackingService.isTracking()) {
					trackingService.stop();
				}
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT)
						.show();
			}
		});

	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = new Intent(MainActivity.this, GPSTrackingService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (updateReceiver == null) {
			updateReceiver = new TrackingUpdateReceiver();
		}
		IntentFilter intentFilter = new IntentFilter(REFRESH_DATA_INTENT);
		registerReceiver(updateReceiver, intentFilter);
	}

	@Override
	protected void onPause() {
		if (updateReceiver != null) {
			unregisterReceiver(updateReceiver);
		}
		super.onPause();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICK_CONTACT_REQUEST) {
			if (resultCode == RESULT_OK) {
				Log.d(TAG, data.toString());
			}
		}
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			trackingService = binder.getService();
			trackingBounded = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			trackingBounded = false;
		}
	};

	private class TrackingUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(REFRESH_DATA_INTENT)) {
				Log.i(TAG, trackingService.getDistance() + "");
			}
		}
	}

}