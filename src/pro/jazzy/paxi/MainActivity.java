
package pro.jazzy.paxi;

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
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
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

    public static final String DEFAULT_AVATAR_URI = "android.resource://pro.jazzy.paxi/drawable/passenger";

    public static final String DEFAULT_MEMBER_AVATAR_URI = "android.resource://pro.jazzy.paxi/drawable/passenger_car";

    public static final String FONT_NAME = "UbuntuM.ttf";

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

    // Font Typeface
    Typeface fontFace;

    PaxiService paxiService;

    TrackingUpdateReceiver updateReceiver;

    ImageView btnAction;

    ImageView btnRouteMode;

    // current state of toggle button
    int currentActionBtnState = ACTION_BUTTON_START;

    // is tracking (service) bounded
    boolean trackingBounded = false;

    long myId;

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

    // View of "Add" element
    View vAddMember;

    Route myRoute;

    /**
     * Create MemeberAdapter instance
     */
    private void createMembersAdapter() {

        membersAdapter = new MembersAdapter(this, myRoute.getMemberNames(), myRoute.getMembers(),
                myRoute, summarizedMembers, this.preferences.getInt("metrics", Route.KILOMETERS));
    }

    /**
     * Create PaymentsAdapter instance
     */
    private void createPaymentsAdapter() {

        paymentsAdapter = new PaymentsAdapter(this, myRoute.getPayments(), myRoute,
                this.preferences.getInt("metrics", Route.KILOMETERS));
    }

    /**
     * Refresh member in car list by recreating MembersAdapter and including it into members LV
     */
    protected void refreshMembersList() {

        createMembersAdapter();
        lvMembersList.setAdapter(membersAdapter);
    }

    /**
     * Refresh payments list by recreating MembersAdapter and including it into payments LV
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

        // Add "Add" row
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        vAddMember = inflater.inflate(R.layout.member_add_element, null, false);
        lvMembersList.addFooterView(vAddMember);

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
    private void putMeOnTheList() {

        Uri uri = ContactsContract.Profile.CONTENT_URI;
        String[] projection = new String[] { ContactsContract.Profile._ID,
                ContactsContract.Profile.DISPLAY_NAME, ContactsContract.Profile.PHOTO_THUMBNAIL_URI };
        String selection = ContactsContract.Profile.IS_USER_PROFILE;
        Cursor profileCursor = managedQuery(uri, projection, selection, null, null);

        profileCursor.moveToFirst();

        myId = Long.parseLong(profileCursor.getString(0));

        Member addMember = new Member(profileCursor.getString(1), Long.parseLong(profileCursor
                .getString(0)));
        addMember.setAvatarUri(profileCursor.getString(2));
        myRoute.memberIn(addMember);

        refreshMembersList();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        this.preferences = getSharedPreferences(APP_PREFERENCES, Activity.MODE_PRIVATE);
        this.fontFace = Typeface.createFromAsset(getAssets(), "fonts/" + FONT_NAME);

        summarizedMembers = new HashMap<Long, Float>();
        myRoute = new Route(this.preferences);

        createMembersListView();
        // by default add phone onwer to members LV
        putMeOnTheList();

        ImageView btnSettings = (ImageView) findViewById(R.id.btnSettings);
        ImageView btnPayment = (ImageView) findViewById(R.id.btnPayment);
        btnRouteMode = (ImageView) findViewById(R.id.btnRouteMode);
        btnAction = (ImageView) findViewById(R.id.btnAction);

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
            Log.i(TAG, "loaded distance from memory " + paxiService.getDistance());
        }

        if (preferences.getBoolean("firstRun", true)) {
            showDialog(DIALOG_SETTINGS);
            SharedPreferences.Editor preferencesEditor = MainActivity.this.preferences.edit();
            preferencesEditor.putBoolean("firstRun", false);
            preferencesEditor.commit();
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
                Member addMember = new Member(data.getExtras().getString("name"),
                        Long.parseLong(data.getExtras().getString("id")));
                addMember.setAvatarUri(data.getExtras().getString("photo"));
                myRoute.memberIn(addMember);
                refreshMembersList();
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

                final int currentMode = preferences.getInt("mode", Route.MIXED_MODE);

                ImageView btnDoneRoute = (ImageView) dialog.findViewById(R.id.btnDoneRoute);
                btnDoneRoute.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        dialog.dismiss();
                    }
                });

                ImageView btnCity = (ImageView) dialog.findViewById(R.id.btnCity);
                btnCity.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        btnRouteMode.setImageResource(R.drawable.btn_menu_mode_city);
                        preferencesEditor.putInt("mode", Route.CITY_MODE);
                        ModeChange cityMode = new ModeChange(Route.CITY_MODE);
                        myRoute.changeMode(cityMode);
                        preferencesEditor.commit();
                        dialog.dismiss();
                    }
                });

                ImageView btnMixed = (ImageView) dialog.findViewById(R.id.btnMixed);
                btnMixed.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        btnRouteMode.setImageResource(R.drawable.btn_menu_mode_mixed);
                        preferencesEditor.putInt("mode", Route.MIXED_MODE);
                        ModeChange mixedMode = new ModeChange(Route.MIXED_MODE);
                        myRoute.changeMode(mixedMode);
                        preferencesEditor.commit();
                        dialog.dismiss();
                    }
                });

                ImageView btnHighway = (ImageView) dialog.findViewById(R.id.btnHighway);
                btnHighway.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        btnRouteMode.setImageResource(R.drawable.btn_menu_mode_highway);
                        preferencesEditor.putInt("mode", Route.HIGHWAY_MODE);
                        ModeChange highwayMode = new ModeChange(Route.HIGHWAY_MODE);
                        myRoute.changeMode(highwayMode);
                        preferencesEditor.commit();
                        dialog.dismiss();
                    }
                });

                switch (currentMode) {
                    case Route.MIXED_MODE:
                        btnMixed.setImageResource(R.drawable.mixed_on);
                        btnCity.setImageResource(R.drawable.btn_mode_city);
                        btnHighway.setImageResource(R.drawable.btn_mode_highway);
                        break;
                    case Route.CITY_MODE:
                        btnCity.setImageResource(R.drawable.city_on);
                        btnMixed.setImageResource(R.drawable.btn_mode_mixed);
                        btnHighway.setImageResource(R.drawable.btn_mode_highway);
                        break;
                    case Route.HIGHWAY_MODE:
                        btnHighway.setImageResource(R.drawable.highway_on);
                        btnMixed.setImageResource(R.drawable.btn_mode_mixed);
                        btnCity.setImageResource(R.drawable.btn_mode_city);
                        break;
                }

                applyGlobalTypeface((RelativeLayout) dialog.findViewById(R.id.rlRouteModeDialog),
                        fontFace);

                break;
            case DIALOG_SETTINGS:

                final EditText etFuelPrice = (EditText) dialog.findViewById(R.id.etFuelPrice);
                final EditText etFuelMixed = (EditText) dialog.findViewById(R.id.etFuelMixed);
                final EditText etFuelCity = (EditText) dialog.findViewById(R.id.etFuelCity);
                final EditText etFuelHighway = (EditText) dialog.findViewById(R.id.etFuelHighway);

                final TextView tvFuelMetrics = (TextView) dialog.findViewById(R.id.tvFuelMetrics);
                final TextView tvMixedMetrics = (TextView) dialog.findViewById(R.id.tvMixedMetrics);
                final TextView tvCityMetrics = (TextView) dialog.findViewById(R.id.tvCityMetrics);
                final TextView tvHighwayMetrics = (TextView) dialog
                        .findViewById(R.id.tvHighwayMetrics);

                int resMetricsLabel;
                if (this.preferences.getInt("metrics", Route.KILOMETERS) == Route.KILOMETERS) {
                    tvFuelMetrics.setText(R.string.per_l);
                    resMetricsLabel = R.string.lkm;
                } else {
                    tvFuelMetrics.setText(R.string.per_g);
                    resMetricsLabel = R.string.mpg;
                }
                tvMixedMetrics.setText(resMetricsLabel);
                tvCityMetrics.setText(resMetricsLabel);
                tvHighwayMetrics.setText(resMetricsLabel);

                final String currentFuelPrice = String.valueOf(this.preferences.getFloat(
                        "fuelPrice", 5.0f));
                final String currentFuelMixed = String.valueOf(this.preferences.getFloat(
                        "fuelMixed", 7.0f));
                final String currentFuelCity = String.valueOf(this.preferences.getFloat("fuelCity",
                        0));
                final String currentFuelHighway = String.valueOf(this.preferences.getFloat(
                        "fuelHighway", 0));

                etFuelPrice.setText(currentFuelPrice);
                etFuelMixed.setText(currentFuelMixed);
                etFuelCity.setText(Float.parseFloat(currentFuelCity) == 0 ? "" : currentFuelCity);
                etFuelHighway.setText(Float.parseFloat(currentFuelHighway) == 0 ? ""
                        : currentFuelHighway);

                TextView tvCurrencyFuel = (TextView) dialog.findViewById(R.id.tvCurrencyFuel);
                tvCurrencyFuel.setText(currency.getSymbol());

                final ImageView btnKm = (ImageView) dialog.findViewById(R.id.btnKm);
                final ImageView btnMiles = (ImageView) dialog.findViewById(R.id.btnMiles);

                if (this.preferences.getInt("metrics", Route.KILOMETERS) == Route.KILOMETERS) {
                    btnKm.setImageResource(R.drawable.metrics_on);
                } else {
                    btnMiles.setImageResource(R.drawable.imperial_on);
                }

                btnKm.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        // set preferences values
                        SharedPreferences.Editor preferencesEditor = MainActivity.this.preferences
                                .edit();
                        preferencesEditor.putInt("metrics", Route.KILOMETERS);
                        preferencesEditor.commit();

                        // set button selected
                        btnMiles.setImageResource(R.drawable.btn_settings_imperial);
                        btnKm.setImageResource(R.drawable.metrics_on);

                        // set units to current view
                        tvMixedMetrics.setText(R.string.lkm);
                        tvCityMetrics.setText(R.string.lkm);
                        tvHighwayMetrics.setText(R.string.lkm);
                        tvFuelMetrics.setText(R.string.per_l);

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
                        preferencesEditor.putInt("metrics", Route.MILES);
                        preferencesEditor.commit();

                        // set button selected
                        btnKm.setImageResource(R.drawable.btn_settings_metrics);
                        btnMiles.setImageResource(R.drawable.imperial_on);

                        // set units to current view
                        tvMixedMetrics.setText(R.string.mpg);
                        tvCityMetrics.setText(R.string.mpg);
                        tvHighwayMetrics.setText(R.string.mpg);
                        tvFuelMetrics.setText(R.string.per_g);

                        // refresh members list
                        refreshMembersList();
                    }
                });

                ImageView btnDoneSettings = (ImageView) dialog.findViewById(R.id.btnDoneSettings);
                btnDoneSettings.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        SharedPreferences.Editor preferencesEditor = MainActivity.this.preferences
                                .edit();

                        String inputValue = etFuelPrice.getText().toString().trim();
                        float floatValue = inputValue.isEmpty() ? 0 : Float.valueOf(inputValue)
                                .floatValue();
                        if (!inputValue.isEmpty() && floatValue != 0) {
                            preferencesEditor.putFloat("fuelPrice", floatValue);
                        } else {
                            AlertDialog.Builder warnDialogBuilder = new Builder(MainActivity.this);
                            warnDialogBuilder.setMessage("You must fill fuel price!");
                            warnDialogBuilder.setPositiveButton("OK", new Dialog.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int jakisInt) {

                                    dialog.dismiss();
                                }
                            });
                            warnDialogBuilder.create().show();
                            return;
                        }

                        inputValue = etFuelMixed.getText().toString().trim();
                        floatValue = inputValue.isEmpty() ? 0 : Float.valueOf(inputValue)
                                .floatValue();
                        if (!inputValue.isEmpty() && floatValue != 0) {
                            preferencesEditor.putFloat("fuelMixed", floatValue);
                        } else {
                            AlertDialog.Builder warnDialogBuilder = new Builder(MainActivity.this);
                            warnDialogBuilder.setMessage("You must fill fuel mixed consumption!");
                            warnDialogBuilder.setPositiveButton("OK", new Dialog.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int jakisInt) {

                                    dialog.dismiss();
                                }
                            });
                            warnDialogBuilder.create().show();
                            return;
                        }

                        inputValue = etFuelCity.getText().toString().trim();
                        floatValue = inputValue.isEmpty() ? 0 : Float.valueOf(inputValue)
                                .floatValue();
                        // if (!inputValue.isEmpty() && floatValue != 0) {
                        preferencesEditor.putFloat("fuelCity", floatValue);
                        // }

                        inputValue = etFuelHighway.getText().toString().trim();
                        floatValue = inputValue.isEmpty() ? 0 : Float.valueOf(inputValue)
                                .floatValue();
                        // if (!inputValue.isEmpty() && floatValue != 0) {
                        preferencesEditor.putFloat("fuelHighway", floatValue);
                        // }

                        preferencesEditor.commit();
                        dialog.dismiss();
                    }
                });

                applyGlobalTypeface((RelativeLayout) dialog.findViewById(R.id.rlSettingsDialog),
                        fontFace);

                break;
            case DIALOG_PAYMENT:

                createPaymentsListView(dialog);

                lvPaymentsList.setOnItemLongClickListener(new OnItemLongClickListener() {

                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
                            final long id) {

                        int[] loc = { 0, 0 };
                        view.getLocationInWindow(loc);
                        int viewLocation = loc[1];
                        ButtonsDialog buttonsDialog = new ButtonsDialog(MainActivity.this,
                                viewLocation, R.drawable.trashbin);
                        buttonsDialog.show();
                        buttonsDialog.setOnAcceptListener(new OnAcceptListener() {

                            @Override
                            public void onAccept(Dialog dialog) {

                                myRoute.removePayment(Payment.getInstance(id));
                                refreshPaymentsList();

                                showToast("Payment removed");
                                dialog.dismiss();
                            }
                        });

                        return false;
                    }
                });

                TextView tvCurrencyPayment = (TextView) dialog.findViewById(R.id.tvCurrencyPayment);
                tvCurrencyPayment.setText(currency.getSymbol());

                final EditText etPayment = (EditText) dialog.findViewById(R.id.etPayment);
                etPayment.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        ((EditText) v).setText("");
                    }
                });

                ImageView btnAddPayment = (ImageView) dialog.findViewById(R.id.btnAddPayment);
                btnAddPayment.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if (paxiService.isTracking()) {
                            String inputValue = etPayment.getText().toString().trim();
                            float floatValue = inputValue.isEmpty() ? 0 : Float.valueOf(inputValue)
                                    .floatValue();

                            if (!inputValue.isEmpty() && floatValue != 0) {
                                Payment payment = new Payment(floatValue);
                                myRoute.addPayment(payment);
                                refreshPaymentsList();
                                showToast("Payment added");
                            }

                            dialog.dismiss();
                        } else {
                            showToast("Start tracking to add payment");
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(etPayment.getWindowToken(), 0);
                        }
                    }
                });

                ImageView btnDonePayment = (ImageView) dialog.findViewById(R.id.btnDonePayment);
                btnDonePayment.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        dialog.dismiss();
                    }
                });

                applyGlobalTypeface((RelativeLayout) dialog.findViewById(R.id.rlPaymentDialog),
                        fontFace);

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
                if (myRoute.getMembers().size() != 0) {
                    if (!paxiService.isTracking()) {
                        paxiService.start();
                        msg = "Tracking on...";
                    } else {
                        msg = "Error! Already tracking!";
                    }
                    bindStopAction();
                } else {
                    msg = "Nobody onboard! Is this UAV?";
                }
                showToast(msg);
                break;
            case ACTION_BUTTON_STOP:
                AlertDialog.Builder promptStopDialogBuilder = new Builder(this);
                promptStopDialogBuilder.setMessage("Do you really want to stop?");
                promptStopDialogBuilder.setNegativeButton("No", new Dialog.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int jakisInt) {

                        dialog.dismiss();
                    }
                });
                promptStopDialogBuilder.setPositiveButton("Yes", new Dialog.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int jakisInt) {

                        String msg = "Tracking off...";
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
                        lvMembersList.removeFooterView(vAddMember);

                        showToast(msg);
                        bindClearAction();

                        dialog.dismiss();
                    }
                });
                promptStopDialogBuilder.create().show();

                break;
            case ACTION_BUTTON_CLEAR:
                AlertDialog.Builder promptClearDialogBuilder = new Builder(this);
                promptClearDialogBuilder.setMessage("All calculations will be lost!");
                promptClearDialogBuilder.setNegativeButton("No", new Dialog.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int jakisInt) {

                        dialog.dismiss();
                    }
                });
                promptClearDialogBuilder.setPositiveButton("Yes", new Dialog.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int jakisInt) {

                        clearRoute();
                        lvMembersList.addFooterView(vAddMember);
                        refreshMembersList();
                        bindStartAction();

                        dialog.dismiss();
                    }
                });
                promptClearDialogBuilder.create().show();

                break;
        }
    }

    /**
     * Bind 'Start' to action button
     */
    private void bindStartAction() {

        btnAction.setImageResource(R.drawable.btn_menu_action_start);
        this.currentActionBtnState = ACTION_BUTTON_START;
    }

    /**
     * Bind 'Stop' to action button
     */
    private void bindStopAction() {

        btnAction.setImageResource(R.drawable.btn_menu_action_stop);
        this.currentActionBtnState = ACTION_BUTTON_STOP;
    }

    /**
     * Bind 'Clear' to action button
     */
    private void bindClearAction() {

        btnAction.setImageResource(R.drawable.btn_menu_action_refresh);
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
    public void onItemClick(AdapterView<?> parent, final View view, int position, final long id) {

        // dirty but pleasant
        if (view.findViewById(R.id.ivAdd) != null) {
            // "Add" clicked
            Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
            intent.putExtra("membersCount", parent.getAdapter().getCount() - 1);
            intent.putExtra("alreadyOnList", myRoute.getMemberIds());
            startActivityForResult(intent, PICK_CONTACT_REQUEST);
            return;
        }

        if (summarizedMembers.containsKey(id)) {
            Log.i(TAG, "Already summarized!");
            return;
        }

        if (paxiService.isTracking()) {
            int viewLocation = view.getTop();
            ButtonsDialog buttonsDialog = new ButtonsDialog(this, viewLocation, R.drawable.leavecar);
            buttonsDialog.show();
            buttonsDialog.setOnAcceptListener(new OnAcceptListener() {

                @Override
                public void onAccept(Dialog dialog) {

                    MemberOut memberOut = new MemberOut(Member.getInstance(id));
                    float toPay = myRoute.memberOut(memberOut);

                    summarizedMembers.put(id, toPay);
                    refreshMembersList();

                    if (myRoute.getMembersCountOnboard() == 0) {
                        if (paxiService.isTracking()) {
                            paxiService.stop();
                        }
                        showToast("Tracking turned off...");
                        bindClearAction();
                        lvMembersList.removeFooterView(vAddMember);
                    }

                    dialog.dismiss();
                }
            });

        } else {
            // showToast("Cannot calculate when not tracking");
        }

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, final View view, int position,
            final long id) {

        if (view.findViewById(R.id.ivAdd) != null) {
            Log.i(TAG, "Nothing to do...");
            return true;
        }

        if (summarizedMembers.containsKey(id)) {
            Log.i(TAG, "You cannot do that! Not now!");
            return false;
        }

        if (paxiService.isTracking()) {
            showToast("Cannot remove member when on road");
            return true;
        }

        int viewLocation = view.getTop();
        ButtonsDialog buttonsDialog = new ButtonsDialog(this, viewLocation, R.drawable.trashbin);
        buttonsDialog.show();
        buttonsDialog.setOnAcceptListener(new OnAcceptListener() {

            @Override
            public void onAccept(Dialog dialog) {

                myRoute.removeMember(Member.getInstance(id));
                refreshMembersList();

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
        putMeOnTheList();
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
        myRoute.setLocation(paxiService.getLastLocation());
        refreshMembersList();
    }

    public void showToast(String msg) {

        Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, getResources()
                .getDimensionPixelSize(R.dimen.toast_offset));
        toast.show();
    }

    public static void applyGlobalTypeface(ViewGroup list, Typeface typeface) {

        for (int i = 0; i < list.getChildCount(); i++) {
            View view = list.getChildAt(i);
            if (view instanceof ViewGroup) {
                applyGlobalTypeface((ViewGroup) view, typeface);
            } else if (view instanceof TextView) {
                ((TextView) view).setTypeface(typeface);
            }
        }
    }
}
