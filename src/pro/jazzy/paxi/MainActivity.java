package pro.jazzy.paxi;

import pro.jazzy.paxi.GPSTrackingService.LocalBinder;
import pro.jazzy.paxi.entity.Serialization;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends Activity implements OnClickListener {

	private static final String TAG = "Paxi";

	static final int PICK_CONTACT_REQUEST = 0;

	static final int ACTION_BUTTON_START = 0;

	static final int ACTION_BUTTON_STOP = 1;

	static final int ACTION_BUTTON_CLEAR = 2;

	static final int DIALOG_ROUTE_MODE = 0;

	static final int DIALOG_SETTINGS = 1;

	static final int DIALOG_PAYMENT = 2;

	private static final String REFRESH_DATA_INTENT = "jazzy_gps_refresh";

	GPSTrackingService trackingService;

	TrackingUpdateReceiver updateReceiver;
	
	Button btnAction;

	int currentActionBtnState = ACTION_BUTTON_START;

	boolean trackingBounded = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		ListView lvMembersList = (ListView) findViewById(R.id.lvMembersList);
		// ArrayAdapter<T> membersAdapter = new A
		// lvMembersList.setAdapter(null);

		Button btnRouteMode = (Button) findViewById(R.id.btnRouteMode);
		Button btnSettings = (Button) findViewById(R.id.btnSettings);
		Button btnPayment = (Button) findViewById(R.id.btnPayment);
		btnAction = (Button) findViewById(R.id.btnAction);

		btnRouteMode.setOnClickListener(this);
		btnSettings.setOnClickListener(this);
		btnPayment.setOnClickListener(this);
		btnAction.setOnClickListener(this);

		/*
		 * Button btnAdd = (Button) findViewById(R.id.btnAdd); Button btnGpsOn =
		 * (Button) findViewById(R.id.btnGpsOn); Button btnGpsOff = (Button)
		 * findViewById(R.id.btnGpsOff);
		 * 
		 * btnAdd.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { Intent intent = new
		 * Intent(MainActivity.this, ContactsActivity.class);
		 * intent.putExtra("membersCount", 4); startActivityForResult(intent,
		 * PICK_CONTACT_REQUEST); } }); btnGpsOn.setOnClickListener(new
		 * OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { String msg; if
		 * (!trackingService.isTracking()) { trackingService.start(); msg =
		 * "Tracking is on..."; } else { msg = "Tracking already started!"; }
		 * Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT)
		 * .show(); } }); btnGpsOff.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { String msg =
		 * "Tracking is off..."; if (trackingService.isTracking()) {
		 * trackingService.stop(); } Toast.makeText(getApplicationContext(),
		 * msg, Toast.LENGTH_SHORT) .show(); } });
		 */

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

	@Override
	protected Dialog onCreateDialog(int id) {
		int layout = 0;
		switch (id) {
		case DIALOG_ROUTE_MODE:
			layout = R.layout.routemode_dialog;
			break;
		case DIALOG_SETTINGS:
			layout = R.layout.settings_dialog;
			break;
		case DIALOG_PAYMENT:
			layout = R.layout.payment_dialog;
			break;
		}
		SettingsDialog dialog = new SettingsDialog(this,
				R.style.DialogFullscreen, layout);
		// dialog.setOnDismissListener(new OnDismissListener() {
		// dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
		return dialog;
	}

	@Override
	protected void onPrepareDialog(int id, final Dialog dialog) {

		Button btnDone = (Button) dialog.findViewById(R.id.btnDone);
		btnDone.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// saving
				dialog.dismiss();
			}
		});

		super.onPrepareDialog(id, dialog);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnRouteMode:
			showDialog(DIALOG_ROUTE_MODE);
			break;
		case R.id.btnSettings:
			showDialog(DIALOG_SETTINGS);
			break;
		case R.id.btnPayment:
			showDialog(DIALOG_PAYMENT);
			break;
		case R.id.btnAction:
			handleActionButton();
			break;
		}
	}

	private void handleActionButton() {
		switch (this.currentActionBtnState) {
		case ACTION_BUTTON_START:
			PaxiUtility.newRoute();
			bindStopAction();
			break;
		case ACTION_BUTTON_STOP:
			bindClearAction();
			break;
		case ACTION_BUTTON_CLEAR:
			bindStartAction();
			break;
		}
	}

	private void bindStartAction() {
		btnAction.setText("Start");
		this.currentActionBtnState = ACTION_BUTTON_START;
	}

	private void bindStopAction() {
		btnAction.setText("Stop");
		this.currentActionBtnState = ACTION_BUTTON_STOP;
	}

	private void bindClearAction() {
		btnAction.setText("Clear");
		this.currentActionBtnState = ACTION_BUTTON_CLEAR;
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
				handleGpsStatusChange();
			}
		}
	}
	
	private void handleGpsStatusChange() {
		Log.i(TAG, trackingService.getDistance() + "");
	}
	
	@Override
	public void onStop() {
		String route = Serialization.routeAsString();
		String members = Serialization.membersAsString();
		
		super.onStop();
    	SharedPreferences sharedPrefs = getSharedPreferences(Serialization.PREF_NAME, 0);
    	SharedPreferences.Editor editor = sharedPrefs.edit();
    	editor.putString("route", route);
    	editor.putString("members", members);
    	editor.commit();
	}
	
	@Override
	public void onRestart() {
		SharedPreferences prefs = getSharedPreferences(Serialization.PREF_NAME, 0);
        String route = prefs.getString("route", "");
		String members = prefs.getString("members", "");
        try {
        	PaxiUtility.CurrentRoute = Serialization.stringToRoute(route);
        	PaxiUtility.members = Serialization.stringToMembers(members);
        } catch(Exception e) {
        	//log or something
        }
        super.onRestart();
	}
}