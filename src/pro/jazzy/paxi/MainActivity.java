package pro.jazzy.paxi;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;

import pro.jazzy.paxi.ButtonsDialog.OnAcceptListener;
import pro.jazzy.paxi.PaxiService.LocalBinder;
import pro.jazzy.paxi.entity.Member;
import pro.jazzy.paxi.entity.MemberOut;
import pro.jazzy.paxi.entity.ModeChange;
import pro.jazzy.paxi.entity.Payment;
import pro.jazzy.paxi.entity.Route;
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

/**
 * Main activity of application
 * 
 * @author Seweryn Zeman <seweryn.zeman@jazzy.pro>
 */
public class MainActivity extends Activity implements OnClickListener,
		OnItemClickListener, OnItemLongClickListener {

	// debug tag
	private static final String TAG = "Paxi";

	// intent identifier for service communication
	private static final String REFRESH_DATA_INTENT = "jazzy_gps_refresh";

	// contacts activity for results
	static final int PICK_CONTACT_REQUEST = 0;

	// toggle button states
	static final int ACTION_BUTTON_START = 0;

	static final int ACTION_BUTTON_STOP = 1;

	static final int ACTION_BUTTON_CLEAR = 2;

	// dialog ids
	static final int DIALOG_ROUTE_MODE = 0;

	static final int DIALOG_SETTINGS = 1;

	static final int DIALOG_PAYMENT = 2;

	// metrics idenfitiers
	static final int KILOMETERS = 0;

	static final int MILES = 1;

	// preferences filename
	static final String APP_PREFERENCES = "paxi.data";

	SharedPreferences preferences = null;

	PaxiService paxiService;

	TrackingUpdateReceiver updateReceiver;

	Button btnAction;

	// current state of toggle button
	int currentActionBtnState = ACTION_BUTTON_START;

	// is tracking (service) bounded
	boolean trackingBounded = false;

	long myId;

	// am I on list
	boolean iAmOnList = true;

	// members already off
	HashMap<Long, Float> summarizedMembers;

	// members adapter
	MembersAdapter membersAdapter;

	// payments adapter
	PaymentsAdapter paymentsAdapter;

	// ListView of members
	ListView lvMembersList;

	// ListView of payments
	ListView lvPaymentsList;

	Route myRoute;

	/**
	 * Create MemeberAdapter instance
	 */
	private void createMembersAdapter() {

		membersAdapter = new MembersAdapter(this, myRoute.getMemberNames(),
				myRoute.getMembers(), myRoute, summarizedMembers,
				this.preferences.getInt("metrics", KILOMETERS));
	}

	/**
	 * Create PaymentsAdapter instance
	 */
	private void createPaymentsAdapter() {

		paymentsAdapter = new PaymentsAdapter(this, myRoute.getPayments(),
				myRoute, this.preferences.getInt("metrics", KILOMETERS));
	}

	/**
	 * Refresh member in car list by recreating MembersAdapter and including it
	 * into members LV
	 */
	protected void refreshMembersList() {

		createMembersAdapter();
		lvMembersList.setAdapter(membersAdapter);
	}

	/**
	 * Refresh payments list by recreating MembersAdapter and including it into
	 * payments LV
	 */
	protected void refreshPaymentsList() {

		createPaymentsAdapter();
		lvPaymentsList.setAdapter(paymentsAdapter);
	}

	/**
	 * Create members LV
	 */
	private void createMembersListView() {

		lvMembersList = (ListView) findViewById(R.id.lvMembersList);

		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View addRow = inflater.inflate(R.layout.member_element, null, false);
		TextView tvNameTemp = (TextView) addRow.findViewById(R.id.tvName);
		tvNameTemp.setText(R.string.add);
		TextView tvCounterTemp = (TextView) addRow.findViewById(R.id.tvCounter);
		// clear unneeded TV
		tvCounterTemp.setText("");
		// set plus as avatar
		ImageView ivAvatarTemp = (ImageView) addRow.findViewById(R.id.ivAvatar);
		ivAvatarTemp.setImageResource(android.R.drawable.btn_plus);

		lvMembersList.addFooterView(addRow);

		lvMembersList.setOnItemClickListener(this);
		lvMembersList.setOnItemLongClickListener(this);

		refreshMembersList();
	}

	/**
	 * Create payments LV
	 */
	private void createPaymentsListView(Dialog dialog) {

		lvPaymentsList = (ListView) dialog.findViewById(R.id.lvPaymentsList);

		// lvPaymentsList.setOnItemLongClickListener(this);

		refreshPaymentsList();
	}

	/**
	 * Get phone owner and add it to the members list
	 */
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

		myId = Long.parseLong(profileCursor.getString(0));

		Member addMember = new Member(profileCursor.getString(1),
				Long.parseLong(profileCursor.getString(0)));
		if (profileCursor.getString(2) != null) {
			addMember.setAvatarUri(profileCursor.getString(2));
		} else {
			// TODO hipek
		}
		myRoute.memberIn(addMember);

		refreshMembersList();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		this.preferences = getSharedPreferences(APP_PREFERENCES,
				Activity.MODE_PRIVATE);

		summarizedMembers = new HashMap<Long, Float>();
		myRoute = new Route(this.preferences);

		createMembersListView();
		// by default add phone onwer to members LV
		getMe();

		Button btnRouteMode = (Button) findViewById(R.id.btnRouteMode);
		Button btnSettings = (Button) findViewById(R.id.btnSettings);
		Button btnPayment = (Button) findViewById(R.id.btnPayment);
		btnAction = (Button) findViewById(R.id.btnAction);

		btnRouteMode.setOnClickListener(this);
		btnSettings.setOnClickListener(this);
		btnPayment.setOnClickListener(this);
		btnAction.setOnClickListener(this);
	}

	@Override
	protected void onStart() {

		super.onStart();
		if (!trackingBounded) {
			Intent intent = new Intent(MainActivity.this, PaxiService.class);
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			Log.i(TAG, "bindService(mConnection) BIND_AUTO_CREATE");
		}
	}

	@Override
	protected void onResume() {

		super.onResume();
		if (updateReceiver == null) {
			Log.i(TAG, "updateReceiver was null so creating new");
			updateReceiver = new TrackingUpdateReceiver();
		}
		IntentFilter intentFilter = new IntentFilter(REFRESH_DATA_INTENT);
		registerReceiver(updateReceiver, intentFilter);
		Log.i(TAG, "registered receiver (updateReceiver)");
		if (trackingBounded) {
			myRoute.setDistance(paxiService.getDistance());
			refreshMembersList();
			Log.i(TAG,
					"loaded distance from memory " + paxiService.getDistance());
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
				Member addMember = new Member(data.getExtras()
						.getString("name"), Long.parseLong(data.getExtras()
						.getString("id")));
				addMember.setAvatarUri(data.getExtras().getString("photo"));
				myRoute.memberIn(addMember);
				refreshMembersList();

				if (Long.parseLong(data.getExtras().getString("id")) == myId) {
					iAmOnList = true;
				}
			}
		}
	}

	@Override
	public void onBackPressed() {

		moveTaskToBack(false);
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

		switch (id) {
		case DIALOG_ROUTE_MODE:

			final SharedPreferences.Editor preferencesEditor = MainActivity.this.preferences
					.edit();

			Button btnDoneRoute = (Button) dialog
					.findViewById(R.id.btnDoneRoute);
			btnDoneRoute.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					dialog.dismiss();
				}
			});

			Button btnCity = (Button) dialog.findViewById(R.id.btnCity);
			btnCity.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					preferencesEditor.putInt("mode", Route.CITY_MODE);
					ModeChange cityMode = new ModeChange(Route.CITY_MODE);
					myRoute.changeMode(cityMode);
					preferencesEditor.commit();
					dialog.dismiss();
				}
			});

			Button btnMixed = (Button) dialog.findViewById(R.id.btnMixed);
			btnMixed.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					preferencesEditor.putInt("mode", Route.MIXED_MODE);
					ModeChange mixedMode = new ModeChange(Route.MIXED_MODE);
					myRoute.changeMode(mixedMode);
					preferencesEditor.commit();
					dialog.dismiss();
				}
			});

			Button btnHighway = (Button) dialog.findViewById(R.id.btnHighway);
			btnHighway.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					preferencesEditor.putInt("mode", Route.HIGHWAY_MODE);
					ModeChange highwayMode = new ModeChange(Route.HIGHWAY_MODE);
					myRoute.changeMode(highwayMode);
					preferencesEditor.commit();
					dialog.dismiss();
				}
			});

			break;
		case DIALOG_SETTINGS:

			final EditText etFuelPrice = (EditText) dialog
					.findViewById(R.id.etFuelPrice);
			final EditText etFuelMixed = (EditText) dialog
					.findViewById(R.id.etFuelMixed);
			final EditText etFuelCity = (EditText) dialog
					.findViewById(R.id.etFuelCity);
			final EditText etFuelHighway = (EditText) dialog
					.findViewById(R.id.etFuelHighway);

			etFuelPrice.setText(String.valueOf(this.preferences.getFloat(
					"fuelPrice", 2.0f)));
			etFuelMixed.setText(String.valueOf(this.preferences.getFloat(
					"fuelMixed", 7.0f)));
			etFuelCity.setText(String.valueOf(this.preferences.getFloat(
					"fuelCity", 0)));
			etFuelHighway.setText(String.valueOf(this.preferences.getFloat(
					"fuelHighway", 0)));

			TextView tvCurrencyFuel = (TextView) dialog
					.findViewById(R.id.tvCurrencyFuel);
			tvCurrencyFuel.setText(currency.getSymbol());

			Button btnKm = (Button) dialog.findViewById(R.id.btnKm);
			Button btnMiles = (Button) dialog.findViewById(R.id.btnMiles);

			btnKm.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					// set preferences values
					SharedPreferences.Editor preferencesEditor = MainActivity.this.preferences
							.edit();
					preferencesEditor.putInt("metrics", KILOMETERS);
					preferencesEditor.commit();

					// set units to current view
					TextView tvMixedMetrics = (TextView) dialog
							.findViewById(R.id.tvMixedMetrics);
					tvMixedMetrics.setText(R.string.lkm);
					TextView tvCityMetrics = (TextView) dialog
							.findViewById(R.id.tvCityMetrics);
					tvCityMetrics.setText(R.string.lkm);
					TextView tvHighwayMetrics = (TextView) dialog
							.findViewById(R.id.tvHighwayMetrics);
					tvHighwayMetrics.setText(R.string.lkm);

					// refresh members list
					refreshMembersList();
				}
			});

			btnMiles.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					// set preferences values
					SharedPreferences.Editor preferencesEditor = MainActivity.this.preferences
							.edit();
					preferencesEditor.putInt("metrics", MILES);
					preferencesEditor.commit();

					// set units to current view
					TextView tvMixedMetrics = (TextView) dialog
							.findViewById(R.id.tvMixedMetrics);
					tvMixedMetrics.setText(R.string.gm);
					TextView tvCityMetrics = (TextView) dialog
							.findViewById(R.id.tvCityMetrics);
					tvCityMetrics.setText(R.string.gm);
					TextView tvHighwayMetrics = (TextView) dialog
							.findViewById(R.id.tvHighwayMetrics);
					tvHighwayMetrics.setText(R.string.gm);

					// refresh members list
					refreshMembersList();
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
							"fuelPrice",
							Float.valueOf(
									etFuelPrice.getText().toString().trim())
									.floatValue());
					preferencesEditor.putFloat(
							"fuelMixed",
							Float.valueOf(
									etFuelMixed.getText().toString().trim())
									.floatValue());
					preferencesEditor.putFloat(
							"fuelCity",
							Float.valueOf(
									etFuelCity.getText().toString().trim())
									.floatValue());
					preferencesEditor.putFloat(
							"fuelHighway",
							Float.valueOf(
									etFuelHighway.getText().toString().trim())
									.floatValue());

					preferencesEditor.commit();
					dialog.dismiss();
				}
			});

			break;
		case DIALOG_PAYMENT:
			createPaymentsListView(dialog);
			
			TextView tvCurrencyPayment = (TextView) dialog
					.findViewById(R.id.tvCurrencyPayment);
			tvCurrencyPayment.setText(currency.getSymbol());

			Button btnAddPayment = (Button) dialog
					.findViewById(R.id.btnAddPayment);
			btnAddPayment.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					EditText etPayment = (EditText) dialog
							.findViewById(R.id.etPayment);
					Payment payment = new Payment(Float.valueOf(
							etPayment.getText().toString().trim()).floatValue());
					myRoute.addPayment(payment);
					refreshPaymentsList();
				}
			});

			Button btnDonePayment = (Button) dialog
					.findViewById(R.id.btnDonePayment);
			btnDonePayment.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					dialog.dismiss();
				}
			});
			break;
		}

		super.onPrepareDialog(id, dialog);
	}

	/**
	 * Handle clicking the action button
	 */
	private void handleActionButton() {

		String msg;
		switch (this.currentActionBtnState) {
		case ACTION_BUTTON_START:
			if (!paxiService.isTracking()) {
				paxiService.start();
				msg = "Tracking on...";
			} else {
				msg = "Already tracking!";
			}
			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT)
					.show();
			bindStopAction();
			break;
		case ACTION_BUTTON_STOP:
			msg = "Tracking off...";
			if (paxiService.isTracking()) {
				paxiService.stop();
			}

			for (int i = 0; i < lvMembersList.getChildCount() - 1; i++) {
				String name = (String) lvMembersList.getItemAtPosition(i);
				long id = lvMembersList.getItemIdAtPosition(i);
				if (!summarizedMembers.containsKey(id)) {
					Member member = new Member(name, id);
					MemberOut memberOut = new MemberOut(member);
					float toPay = myRoute.memberOut(memberOut);
					summarizedMembers.put(id, toPay);
				}
			}
			refreshMembersList();

			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT)
					.show();
			bindClearAction();
			break;
		case ACTION_BUTTON_CLEAR:
			clearRoute();
			bindStartAction();
			break;
		}
	}

	/**
	 * Bind 'Start' to action button
	 */
	private void bindStartAction() {

		btnAction.setText("Start");
		this.currentActionBtnState = ACTION_BUTTON_START;
	}

	/**
	 * Bind 'Stop' to action button
	 */
	private void bindStopAction() {

		btnAction.setText("Stop");
		this.currentActionBtnState = ACTION_BUTTON_STOP;
	}

	/**
	 * Bind 'Clear' to action button
	 */
	private void bindClearAction() {

		btnAction.setText("Clear");
		this.currentActionBtnState = ACTION_BUTTON_CLEAR;
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
	public void onItemClick(AdapterView<?> parent, final View view,
			int position, final long id) {

		if ((position + 1) == parent.getChildCount()) {
			// "Add" clicked
			Intent intent = new Intent(MainActivity.this,
					ContactsActivity.class);
			intent.putExtra("membersCount", parent.getChildCount() - 1);
			intent.putExtra("iAmOnList", iAmOnList);
			startActivityForResult(intent, PICK_CONTACT_REQUEST);
			return;
		}

		if (summarizedMembers.containsKey(id)) {
			Log.i(TAG, "Already summarized!");
			return;
		}

		if (paxiService.isTracking()) {
			int viewLocation = view.getTop();
			ButtonsDialog buttonsDialog = new ButtonsDialog(this, viewLocation);
			buttonsDialog.show();
			buttonsDialog.setOnAcceptListener(new OnAcceptListener() {

				@Override
				public void onAccept(Dialog dialog) {
					TextView tvName = (TextView) view.findViewById(R.id.tvName);
					String name = tvName.getText().toString(); // TODO to id

					Member member = new Member(name, id);

					MemberOut memberOut = new MemberOut(member);
					float toPay = myRoute.memberOut(memberOut);

					summarizedMembers.put(id, toPay);
					refreshMembersList();

					dialog.dismiss();

					if (myRoute.getMembersCountOnboard() == 0) {
						if (paxiService.isTracking()) {
							paxiService.stop();
						}
						Toast.makeText(getApplicationContext(),
								"Tracking is off...", Toast.LENGTH_SHORT)
								.show();
						bindClearAction();
					}
				}
			});
		} else {
			Toast.makeText(getApplicationContext(),
					"Cannot calculate when not tracking...", Toast.LENGTH_SHORT)
					.show();
		}

	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, final View view,
			int position, final long id) {

		if (position == parent.getAdapter().getCount() - 1) {
			Log.i(TAG, "Nothing to do with 'Add'...");
			return false;
		}

		int viewLocation = view.getTop();
		ButtonsDialog buttonsDialog = new ButtonsDialog(this, viewLocation);
		buttonsDialog.show();
		buttonsDialog.setOnAcceptListener(new OnAcceptListener() {

			@Override
			public void onAccept(Dialog dialog) {
				TextView tvName = (TextView) view.findViewById(R.id.tvName);
				String name = tvName.getText().toString(); // TODO to id

				if (id == myId) {
					iAmOnList = false;
				}

				Member member = new Member(name, id);

				myRoute.removeMember(member);
				refreshMembersList();

				Toast.makeText(getApplicationContext(), name + " removed!",
						Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}
		});

		return true;
	}

	/**
	 * Clear all about router - adapter, route object...
	 */
	private void clearRoute() {

		summarizedMembers.clear();
		paxiService.clear();
		myRoute = new Route(this.preferences);
		getMe();
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {

			LocalBinder binder = (LocalBinder) service;
			paxiService = binder.getService();
			trackingBounded = true;
			Log.i(TAG, "trackingBounded=true");
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {

			trackingBounded = false;
			Log.i(TAG, "trackingBounded=false");
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

	/**
	 * Handle GPS receive intent
	 */
	private void handleGpsStatusChange() {

		myRoute.addDistance(paxiService.getDistanceDelta());
		refreshMembersList();
	}

}
