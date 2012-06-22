
package pro.jazzy.paxi;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import pro.jazzy.paxi.PaxiService.LocalBinder;
import pro.jazzy.paxi.entity.Member;
import android.app.Activity;
import android.app.AlertDialog.Builder;
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
public class MainActivity extends Activity implements OnClickListener, OnItemClickListener,
        OnItemLongClickListener {

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

    // am I on list
    boolean iAmOnList = true;

    // members already off
    ArrayList<Long> summariedMembers;

    // members adapter
    MembersAdapter membersAdapter;

    // ListView of members
    ListView lvMembersList;

    /**
     * Create MemeberAdapter instance
     */
    private void createMembersAdapter() {

        membersAdapter = new MembersAdapter(this, PaxiUtility.getMemberNames(),
                PaxiUtility.getMembers());
    }

    /**
     * Refresh member in car list by recreating MembersAdapter and including it into members LV
     */
    private void refreshMembersList() {

        createMembersAdapter();
        lvMembersList.setAdapter(membersAdapter);
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

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        summariedMembers = new ArrayList<Long>();
        PaxiUtility.newRoute();

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

        this.preferences = getSharedPreferences(APP_PREFERENCES, Activity.MODE_PRIVATE);
    }

    @Override
    protected void onStart() {

        super.onStart();
        Intent intent = new Intent(MainActivity.this, PaxiService.class);
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
            PaxiUtility.currentRoute.setCurrentDistance(paxiService.getDistance());
            refreshMembersList();
            Log.i(TAG, "loaded distance from memory " + paxiService.getDistance());
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
                Member addMember = PaxiUtility.memberIn(data.getExtras().getString("name"));
                addMember.setPhotoUri(data.getExtras().getString("photo"));
                addMember.setId(Long.parseLong(data.getExtras().getString("id")));
                refreshMembersList();
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
        SettingsDialog dialog = new SettingsDialog(this, R.style.DialogFullscreen, layout);
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

                Button btnDoneRoute = (Button) dialog.findViewById(R.id.btnDoneRoute);
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

                        preferencesEditor.putString("mode", "city");
                        PaxiUtility.changeRouteType(PaxiUtility.ROUTE_TYPE_CITY);
                        preferencesEditor.commit();
                        dialog.dismiss();
                    }
                });

                Button btnMixed = (Button) dialog.findViewById(R.id.btnMixed);
                btnMixed.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        preferencesEditor.putString("mode", "mixed");
                        PaxiUtility.changeRouteType(PaxiUtility.ROUTE_TYPE_MIXED);
                        preferencesEditor.commit();
                        dialog.dismiss();
                    }
                });

                Button btnHighway = (Button) dialog.findViewById(R.id.btnHighway);
                btnHighway.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        preferencesEditor.putString("mode", "highway");
                        PaxiUtility.changeRouteType(PaxiUtility.ROUTE_TYPE_HIGHWAY);
                        preferencesEditor.commit();
                        dialog.dismiss();
                    }
                });

                break;
            case DIALOG_SETTINGS:

                final EditText etFuelCity = (EditText) dialog.findViewById(R.id.etFuelCity);
                final EditText etFuelMixed = (EditText) dialog.findViewById(R.id.etFuelMixed);
                final EditText etFuelHighway = (EditText) dialog.findViewById(R.id.etFuelHighway);
                final EditText etFuelPrice = (EditText) dialog.findViewById(R.id.etFuelPrice);

                etFuelMixed.setText(String.valueOf(this.preferences.getFloat("fuelMixed", 7.0f)));
                etFuelCity.setText(String.valueOf(this.preferences.getFloat("fuelCity", 0)));
                etFuelHighway.setText(String.valueOf(this.preferences.getFloat("fuelHighwawy", 0)));
                etFuelPrice.setText(String.valueOf(this.preferences.getFloat("fuelPrice", 2.0f)));

                TextView tvCurrencyFuel = (TextView) dialog.findViewById(R.id.tvCurrencyFuel);
                tvCurrencyFuel.setText(currency.getSymbol());

                Button btnKm = (Button) dialog.findViewById(R.id.btnKm);
                Button btnMiles = (Button) dialog.findViewById(R.id.btnMiles);

                btnKm.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        // set preferences values
                        SharedPreferences.Editor preferencesEditor = MainActivity.this.preferences
                                .edit();
                        preferencesEditor.putBoolean("lkm", true);
                        preferencesEditor.putBoolean("gm", false);
                        preferencesEditor.commit();

                        // set units to current view
                        TextView tvMixedMetrics = (TextView) dialog
                                .findViewById(R.id.tvMixedMetrics);
                        tvMixedMetrics.setText(R.string.lkm);
                        TextView tvCityMetrics = (TextView) dialog.findViewById(R.id.tvCityMetrics);
                        tvCityMetrics.setText(R.string.lkm);
                        TextView tvHighwayMetrics = (TextView) dialog
                                .findViewById(R.id.tvHighwayMetrics);
                        tvHighwayMetrics.setText(R.string.lkm);
                    }
                });

                btnMiles.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        // set preferences values
                        SharedPreferences.Editor preferencesEditor = MainActivity.this.preferences
                                .edit();
                        preferencesEditor.putBoolean("lkm", false);
                        preferencesEditor.putBoolean("gm", true);
                        preferencesEditor.commit();

                        // set units to current view
                        TextView tvMixedMetrics = (TextView) dialog
                                .findViewById(R.id.tvMixedMetrics);
                        tvMixedMetrics.setText(R.string.gm);
                        TextView tvCityMetrics = (TextView) dialog.findViewById(R.id.tvCityMetrics);
                        tvCityMetrics.setText(R.string.gm);
                        TextView tvHighwayMetrics = (TextView) dialog
                                .findViewById(R.id.tvHighwayMetrics);
                        tvHighwayMetrics.setText(R.string.gm);
                    }
                });

                Button btnDoneSettings = (Button) dialog.findViewById(R.id.btnDoneSettings);
                btnDoneSettings.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        SharedPreferences.Editor preferencesEditor = MainActivity.this.preferences
                                .edit();

                        preferencesEditor
                                .putFloat("fuelPrice",
                                        Float.valueOf(etFuelPrice.getText().toString().trim())
                                                .floatValue());
                        preferencesEditor
                                .putFloat("fuelMixed",
                                        Float.valueOf(etFuelMixed.getText().toString().trim())
                                                .floatValue());
                        preferencesEditor.putFloat("fuelCity",
                                Float.valueOf(etFuelCity.getText().toString().trim()).floatValue());
                        preferencesEditor.putFloat("fuelHighway",
                                Float.valueOf(etFuelHighway.getText().toString().trim())
                                        .floatValue());

                        preferencesEditor.commit();
                        dialog.dismiss();
                    }
                });

                break;
            case DIALOG_PAYMENT:
                TextView tvCurrencyPayment = (TextView) dialog.findViewById(R.id.tvCurrencyPayment);
                tvCurrencyPayment.setText(currency.getSymbol());

                Button btnDonePayment = (Button) dialog.findViewById(R.id.btnDonePayment);
                btnDonePayment.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        EditText etPayment = (EditText) dialog.findViewById(R.id.etPayment);
                        float payment = (Float.valueOf(etPayment.getText().toString().trim())
                                .floatValue());
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        TextView tvName = (TextView) view.findViewById(R.id.tvName);
        String toOut = tvName.getText().toString(); // TODO by ID?

        if ((position + 1) == parent.getChildCount()) {
            // "Add" clicked
            Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
            intent.putExtra("membersCount", parent.getChildCount() - 1);
            intent.putExtra("iAmOnList", iAmOnList);
            startActivityForResult(intent, PICK_CONTACT_REQUEST);
            return;
        }

        int viewLocation = view.getTop();
        ButtonsDialog buttonsDialog = new ButtonsDialog(this, R.style.DialogButtons,
                R.layout.double_buttons, viewLocation);
        buttonsDialog.show();
        // TODO finish

        if (summariedMembers.indexOf(id) != -1) {
            Log.d(TAG, "already done!");
            return;
        }

        float toPay = PaxiUtility.memberOut(toOut); // TODO CALC!!!
        summariedMembers.add(id);

        // TODO update TVs!
        Toast.makeText(getApplicationContext(), "toPay=" + toPay, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        if (position == parent.getAdapter().getCount() - 1) {
            // if long-clicked "Add"
            return false;
        }

        int viewLocation = view.getTop();
        ButtonsDialog buttonsDialog = new ButtonsDialog(this, R.style.DialogButtons,
                R.layout.double_buttons, viewLocation);
        buttonsDialog.show();
        // TODO finish
        if (true) return false;

        if (position == (parent.getAdapter().getCount() - 2)) {
            iAmOnList = false;
        }

        TextView tvName = (TextView) view.findViewById(R.id.tvName);
        String toRemove = tvName.getText().toString(); // TODO by ID?

        PaxiUtility.removeMember(toRemove);
        refreshMembersList();

        Toast.makeText(getApplicationContext(), toRemove + " removed!", Toast.LENGTH_SHORT).show();

        return false;
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
                    msg = "Tracking is on...";
                } else {
                    msg = "Tracking already started!";
                }
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                bindStopAction();
                break;
            case ACTION_BUTTON_STOP:
                msg = "Tracking is off...";
                if (paxiService.isTracking()) {
                    paxiService.stop();
                }
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
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

    /**
     * Get phone owner and add it to the members list
     */
    private void getMe() {

        String[] row = new String[3];
        Uri uri = ContactsContract.Profile.CONTENT_URI;
        String[] projection = new String[] { ContactsContract.Profile._ID,
                ContactsContract.Profile.DISPLAY_NAME, ContactsContract.Profile.PHOTO_THUMBNAIL_URI };
        String selection = ContactsContract.Profile.IS_USER_PROFILE;
        Cursor profileCursor = managedQuery(uri, projection, selection, null, null);

        profileCursor.moveToFirst();
        row[0] = profileCursor.getString(0); // id
        row[1] = profileCursor.getString(1); // name
        row[2] = profileCursor.getString(2); // photo uri
        Member addMember = PaxiUtility.memberIn(profileCursor.getString(1));
        if (profileCursor.getString(2) != null) {
            addMember.setPhotoUri(profileCursor.getString(2));
        } else {
            // TODO hipek
        }
        addMember.setId(Long.parseLong(profileCursor.getString(0)));

        refreshMembersList();
    }

    /**
     * Clear all about router - adapter, route object...
     */
    private void clearRoute() {

        PaxiUtility.newRoute(); // TODO check if is setting all over agagin
        // TODO clear service storage
        getMe();
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            LocalBinder binder = (LocalBinder) service;
            paxiService = binder.getService();
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

    /**
     * Handle GPS receive intent
     */
    private void handleGpsStatusChange() {

        PaxiUtility.addDistance(paxiService.getDistanceDelta());
        refreshMembersList();
        Log.i(TAG, "total dist GPS: " + paxiService.getDistance());
    }

}
