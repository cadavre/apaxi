package pro.jazzy.paxi;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import pro.jazzy.paxi.GPSTrackingService.LocalBinder;
import pro.jazzy.paxi.entity.Member;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener,
		OnItemClickListener, OnItemLongClickListener {

	private static final String TAG = "Paxi";

	static final int PICK_CONTACT_REQUEST = 0;

	static final int ACTION_BUTTON_START = 0;

	static final int ACTION_BUTTON_STOP = 1;

	static final int ACTION_BUTTON_CLEAR = 2;

	static final int DIALOG_ROUTE_MODE = 0;

	static final int DIALOG_SETTINGS = 1;

	static final int DIALOG_PAYMENT = 2;

	static final String APP_PREFERENCES = "paxi.data";

	SharedPreferences preferences = null;

	private static final String REFRESH_DATA_INTENT = "jazzy_gps_refresh";

	GPSTrackingService trackingService;

	TrackingUpdateReceiver updateReceiver;

	Button btnAction;

	int currentActionBtnState = ACTION_BUTTON_START;

	boolean trackingBounded = false;

	boolean iAmOnList = true;
	
	ArrayList<Long> summariedMembers;

	ArrayAdapter<String> membersAdapter;

	ListView lvMembersList;

	private void createMembersListView() {
		lvMembersList = (ListView) findViewById(R.id.lvMembersList);

		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.member_element, null, false);
		TextView tvNameTemp = (TextView) rowView.findViewById(R.id.tvName);
		tvNameTemp.setText("Add");
		TextView tvCounterTemp = (TextView) rowView
				.findViewById(R.id.tvCounter);
		tvCounterTemp.setText("");
		ImageView ivAvatarTemp = (ImageView) rowView
				.findViewById(R.id.ivAvatar);
		ivAvatarTemp.setImageResource(android.R.drawable.btn_plus);

		lvMembersList.addFooterView(rowView);

		lvMembersList.setOnItemClickListener(this);
		lvMembersList.setOnItemLongClickListener(this);

		createMembersAdapter();
		lvMembersList.setAdapter(membersAdapter);
	}

	private void createMembersAdapter() {
		membersAdapter = new MembersAdapter(this, PaxiUtility.getMemberNames(),
				PaxiUtility.getMembers());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		summariedMembers = new ArrayList<Long>();
		
		PaxiUtility.newRoute(getApplicationContext());

		createMembersListView();

		Button btnRouteMode = (Button) findViewById(R.id.btnRouteMode);
		Button btnSettings = (Button) findViewById(R.id.btnSettings);
		Button btnPayment = (Button) findViewById(R.id.btnPayment);
		btnAction = (Button) findViewById(R.id.btnAction);

		btnRouteMode.setOnClickListener(this);
		btnSettings.setOnClickListener(this);
		btnPayment.setOnClickListener(this);
		btnAction.setOnClickListener(this);

		this.getMe();

		this.preferences = getSharedPreferences(APP_PREFERENCES,
				Activity.MODE_PRIVATE);
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
		if (trackingBounded) {
			PaxiUtility.CurrentRoute.setCurrentDistance(trackingService
					.getDistance());
			createMembersAdapter();
			lvMembersList.setAdapter(membersAdapter);
			Log.i(TAG,
					"loaded distance from memory "
							+ trackingService.getDistance());
		}
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
				Member lastMember = PaxiUtility.memberIn(data.getExtras()
						.getString("name"));
				lastMember.setPhotoUri(data.getExtras().getString("photo"));
				createMembersAdapter();
				lvMembersList.setAdapter(membersAdapter);
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
	protected void onPrepareDialog(final int id, final Dialog dialog) {

		Currency currency = Currency.getInstance(Locale.getDefault());

		final EditText etFuelCity = (EditText) dialog
				.findViewById(R.id.etFuelCity);
		final EditText etFuelHighway = (EditText) dialog
				.findViewById(R.id.etFuelHighway);
		final EditText etFuelPrice = (EditText) dialog
				.findViewById(R.id.etFuelPrice);

		switch (id) {
		case DIALOG_ROUTE_MODE:

			Button btnCity = (Button) dialog.findViewById(R.id.btnCity);
			btnCity.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					SharedPreferences.Editor preferencesEditor = MainActivity.this.preferences
							.edit();
					preferencesEditor.putString("mode", "city");
					preferencesEditor.commit();
					PaxiUtility.changeRouteType(PaxiUtility.ROUTE_TYPE_CITY);
					dialog.dismiss();
				}
			});

			Button btnMixed = (Button) dialog.findViewById(R.id.btnMixed);
			btnMixed.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					SharedPreferences.Editor preferencesEditor = MainActivity.this.preferences
							.edit();
					preferencesEditor.putString("mode", "mixed");
					preferencesEditor.commit();
					PaxiUtility.changeRouteType(PaxiUtility.ROUTE_TYPE_MIXED);
					dialog.dismiss();
				}
			});

			Button btnHighway = (Button) dialog.findViewById(R.id.btnHighway);
			btnHighway.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					SharedPreferences.Editor preferencesEditor = MainActivity.this.preferences
							.edit();
					preferencesEditor.putString("mode", "highway");
					preferencesEditor.commit();
					PaxiUtility.changeRouteType(PaxiUtility.ROUTE_TYPE_HIGHWAY);
					dialog.dismiss();
				}
			});

			Button btnDoneRoute = (Button) dialog
					.findViewById(R.id.btnDoneRoute);
			btnDoneRoute.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});

			break;
		case DIALOG_SETTINGS:

			etFuelCity.setText(String.valueOf(this.preferences.getFloat(
					"etFuelCity", 9.0f)));
			etFuelHighway.setText(String.valueOf(this.preferences.getFloat(
					"etFuelHighway", 7.0f)));
			etFuelPrice.setText(String.valueOf(this.preferences.getFloat(
					"etFuelPrice", 5.9f)));

			TextView tvCurrencyFuel = (TextView) dialog
					.findViewById(R.id.tvCurrencyFuel);
			tvCurrencyFuel.setText(currency.getSymbol());

			Button btnKm = (Button) dialog.findViewById(R.id.btnKm);
			Button btnMiles = (Button) dialog.findViewById(R.id.btnMiles);

			btnKm.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					SharedPreferences.Editor preferencesEditor = MainActivity.this.preferences
							.edit();
					preferencesEditor.putBoolean("lkm", true);
					preferencesEditor.putBoolean("gl", false);
					preferencesEditor.commit();

					TextView tvCityMetrics = (TextView) dialog
							.findViewById(R.id.tvCityMetrics);
					tvCityMetrics.setText("l/100km");
					TextView tvHighwayMetrics = (TextView) dialog
							.findViewById(R.id.tvHighwayMetrics);
					tvHighwayMetrics.setText("l/100km");
				}
			});

			btnMiles.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					SharedPreferences.Editor preferencesEditor = MainActivity.this.preferences
							.edit();
					preferencesEditor.putBoolean("lkm", false);
					preferencesEditor.putBoolean("gl", true);
					preferencesEditor.commit();

					TextView tvCityMetrics = (TextView) dialog
							.findViewById(R.id.tvCityMetrics);
					tvCityMetrics.setText("gal/100m");
					TextView tvHighwayMetrics = (TextView) dialog
							.findViewById(R.id.tvHighwayMetrics);
					tvHighwayMetrics.setText("gal/100m");
				}
			});

			Button btnDoneSettings = (Button) dialog
					.findViewById(R.id.btnDoneSettings);
			btnDoneSettings.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					SharedPreferences.Editor preferencesEditor = MainActivity.this.preferences
							.edit();
					preferencesEditor.putFloat(
							"etFuelCity",
							Float.valueOf(
									etFuelCity.getText().toString().trim())
									.floatValue());
					preferencesEditor.putFloat(
							"etFuelHighway",
							Float.valueOf(
									etFuelHighway.getText().toString().trim())
									.floatValue());
					preferencesEditor.putFloat(
							"etFuelPrice",
							Float.valueOf(
									etFuelPrice.getText().toString().trim())
									.floatValue());
					preferencesEditor.commit();
					dialog.dismiss();
				}
			});

			break;
		case DIALOG_PAYMENT:
			TextView tvCurrencyPayment = (TextView) dialog
					.findViewById(R.id.tvCurrencyPayment);
			tvCurrencyPayment.setText(currency.getSymbol());

			Button btnDonePayment = (Button) dialog
					.findViewById(R.id.btnDonePayment);
			btnDonePayment.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					EditText etPayment = (EditText) dialog
							.findViewById(R.id.etPayment);
					int payment = (int) (Float.valueOf(
							etPayment.getText().toString().trim()).floatValue() * 100);
					PaxiUtility.addPayment(payment);
					dialog.dismiss();
				}
			});
			break;
		}

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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		TextView tvName = (TextView) view.findViewById(R.id.tvName);
		String toOut = tvName.getText().toString();

		if ((position + 1) == parent.getChildCount()) {
			Intent intent = new Intent(MainActivity.this,
					ContactsActivity.class);
			intent.putExtra("membersCount", parent.getChildCount() - 1);
			intent.putExtra("iAmOnList", iAmOnList);
			// TODO check if I'm on the list
			startActivityForResult(intent, PICK_CONTACT_REQUEST);
			return;
		}
		
		if (summariedMembers.indexOf(id) != -1) {
			Log.d(TAG, "already done!");
			return;
		}

		float toPay = PaxiUtility.memberOut(toOut);
		summariedMembers.add(id);
		Toast.makeText(getApplicationContext(), "toPay=" + toPay,
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		TextView tvName = (TextView) view.findViewById(R.id.tvName);
		String toRemove = tvName.getText().toString();
		PaxiUtility.removeMember(toRemove);
		createMembersAdapter();
		lvMembersList.setAdapter(membersAdapter);
		Toast.makeText(getApplicationContext(), toRemove + " removed!",
				Toast.LENGTH_SHORT).show();
		if (position == (parent.getAdapter().getCount() - 2)) {
			iAmOnList = false;
		}
		return false;
	}

	private void handleActionButton() {
		String msg;
		switch (this.currentActionBtnState) {
		case ACTION_BUTTON_START:
			if (!trackingService.isTracking()) {
				trackingService.start();
				msg = "Tracking is on...";
			} else {
				msg = "Tracking already started!";
			}
			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT)
					.show();
			bindStopAction();
			break;
		case ACTION_BUTTON_STOP:
			msg = "Tracking is off...";
			if (trackingService.isTracking()) {
				trackingService.stop();
			}
			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT)
					.show();
			bindClearAction();
			break;
		case ACTION_BUTTON_CLEAR:
			clearAdapter();
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

	private void getMe() {
		String[] row = new String[3];
		Uri uri = ContactsContract.Profile.CONTENT_URI;
		String[] projection = new String[] { ContactsContract.Profile._ID,
				ContactsContract.Profile.DISPLAY_NAME,
				ContactsContract.Profile.PHOTO_THUMBNAIL_URI };
		String selection = ContactsContract.Profile.IS_USER_PROFILE;
		Cursor profileCursor = managedQuery(uri, projection, selection, null,
				null);

		profileCursor.moveToFirst();
		row[0] = profileCursor.getString(0); // id
		row[1] = profileCursor.getString(1); // name
		row[2] = profileCursor.getString(2); // photo uri
		Member lastMember = PaxiUtility.memberIn(profileCursor.getString(1));
		lastMember.setPhotoUri(profileCursor.getString(2));

		createMembersAdapter();
		lvMembersList.setAdapter(membersAdapter);
	}

	private void clearAdapter() {
		PaxiUtility.newRoute(getApplicationContext());
		getMe();
		createMembersAdapter();
		lvMembersList.setAdapter(membersAdapter);
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
		PaxiUtility.addDistance(trackingService.getDistanceDelta());
		createMembersAdapter();
		lvMembersList.setAdapter(membersAdapter);
		Log.i(TAG, "total dist GPS: " + trackingService.getDistance());
	}

}