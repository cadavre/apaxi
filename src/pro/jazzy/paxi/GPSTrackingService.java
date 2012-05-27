package pro.jazzy.paxi;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class GPSTrackingService extends Service {

	private static final String TAG = "Paxi GPS";

	private static final String REFRESH_DATA_INTENT = "jazzy_gps_refresh";

	private static final int NOTIFY_ID = 1;

	private static final int LAST_LOCATIONS = 5;

	private final IBinder mBinder = new LocalBinder();

	NotificationManager notificationManager;

	LocationManager locationManager;

	LocationListener locationListener;

	private ArrayList<Location> lastLocations;

	private int distance = 0;
	
	private int distanceDelta = 0;

	private boolean tracking = false;

	@Override
	public void onCreate() {
		super.onCreate();
		lastLocations = new ArrayList<Location>();
		lastLocations.ensureCapacity(LAST_LOCATIONS + 1);
		String ns = getApplicationContext().NOTIFICATION_SERVICE;
		String ls = getApplicationContext().LOCATION_SERVICE;
		notificationManager = (NotificationManager) getSystemService(ns);
		locationManager = (LocationManager) getSystemService(ls);
	}

	public class LocalBinder extends Binder {
		GPSTrackingService getService() {
			return GPSTrackingService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		lastLocations.clear();
		return mBinder;
	}

	public void start() {
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				handleLocationChange(location);
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				Log.d(TAG, "onStatusChanged, " + status + " of " + provider);
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, locationListener);

		notifyShowTracking();
		this.tracking = true;
	}

	public void stop() {
		locationManager.removeUpdates(locationListener);
		notifyHideTracking();
		this.tracking = false;
	}

	private void handleLocationChange(Location location) {
		int distance = 0;
		int accuracy = (int) location.getAccuracy();
		if (lastLocations.size() != 0) {
			distance = (int) location.distanceTo(lastLocations
					.get(lastLocations.size() - 1));
		}

		lastLocations.add(location);
		if ((lastLocations.size() > LAST_LOCATIONS) && (distance > accuracy)) {
			lastLocations = new ArrayList<Location>(lastLocations.subList(1, 6));
			this.distanceDelta = distance;
			this.distance += distance;
			sendBroadcast(new Intent(REFRESH_DATA_INTENT));
		}
	}

	public int getDistance() {
		return this.distance;
	}
	
	public int getDistanceDelta() {
		return this.distanceDelta;
	}
	
	public boolean isTracking() {
		return this.tracking;
	}

	private void notifyShowTracking() {
		int icon = android.R.drawable.ic_menu_compass;
		CharSequence tickerText = "Paxi GPS recording ON!";
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);
		CharSequence contentTitle = "Paxi";
		CharSequence contentText = "GPS Recording ON";
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(getApplicationContext(), contentTitle,
				contentText, contentIntent);
		notification.flags = Notification.FLAG_NO_CLEAR;
		notificationManager.notify(NOTIFY_ID, notification);
	}

	private void notifyHideTracking() {
		notificationManager.cancel(NOTIFY_ID);
	}
}