package com.umang.dashnotifier;

import java.lang.reflect.Field;
import java.util.ArrayList;


import com.umang.dashnotifier.provider.NotifSQLiteHelper;
import com.umang.dashnotifier.provider.NotificationProvider;
import com.umang.dashnotifier.provider.NotificationStore;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.RemoteViews;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class DashNotificationListener extends NotificationListenerService
		implements SensorEventListener {

	private static final String TAG = "DashNotificationListener";

	ContentResolver resolver;
	String[] projection;
	ArrayList<String> notifText;
	SharedPreferences preferences;
	boolean screen_on_pref;
	boolean proximity_pref;
	PowerManager pm;
	Uri mUri;
	private SensorManager sensorManager;
	private Sensor proximity;
	private float maxRange;
	private float sensorValue;

	@Override
	public void onCreate() {
		super.onCreate();
		projection = NotificationStore.allColumns;
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		if (proximity != null)
			maxRange = proximity.getMaximumRange();
		Log.v(TAG, "Service created");
		int count = getContentResolver().delete(
				NotificationProvider.CONTENT_URI, null, null);
		Log.v(TAG, "Deleted on create: " + Integer.toString(count));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "Service Destroyed");
	}

	@SuppressLint("Wakelock")
	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {

		Log.v(TAG,
				"Posted by: " + sbn.getPackageName() + ": "
						+ Integer.toString(sbn.getId()));
		//Log.v(TAG, Integer.toString(sbn.getNotification().icon));
		getPackageManager().getDrawable(sbn.getPackageName(),sbn.getNotification().icon, null);
		
		CharSequence ticker = sbn.getNotification().tickerText;

		int extNumber = extMatch(sbn.getPackageName());
		
		if (isDisplayed(Integer.toString(extNumber),sbn.isClearable()) ) {
			String nTime = DateFormat
					.format("hh:mm", sbn.getPostTime() * 1000L).toString();
			screen_on_pref = preferences.getBoolean(
					"turn_screen_on" + Integer.toString(extNumber), false);
			proximity_pref = preferences.getBoolean(
					"proximity_check" + Integer.toString(extNumber), false);

			if (!pm.isScreenOn() && screen_on_pref) {
				if (proximity_pref) {
					sensorManager.registerListener(this, proximity,
							SensorManager.SENSOR_DELAY_FASTEST);
					Log.v(TAG, "Sensor value:" + Float.toString(sensorValue)
							+ ", Max range: " + Float.toString(maxRange));
					if (sensorValue >= Float
							.parseFloat(getString(R.string.proximityThreshold))) {
						turnMeOn();

					}
					sensorManager.unregisterListener(this);
				} else
					turnMeOn();
			}

			notifText = extractor(sbn.getNotification());

			Log.v(TAG, "In listener: " + notifText.toString());

			//2 - For title and text
			if (notifText.size() >= 2) {
				Cursor countCheck = getContentResolver().query(
						NotificationProvider.CONTENT_URI,
						projection,
						NotifSQLiteHelper.COL_PNAME + " = ? AND "
								+ NotifSQLiteHelper.COL_NOTIF_ID + " = ? ",
						new String[] { sbn.getPackageName(),
								Integer.toString(sbn.getId()) }, null);
				countCheck.moveToFirst();
				if (countCheck.getCount() == 0) {
					try {
						mUri = getContentResolver().insert(
								NotificationProvider.CONTENT_URI,
								createContentValue(sbn.getPackageName(), sbn
										.getId(), notifText, nTime,
										ticker != null ? ticker.toString() : null,
										sbn.isClearable(), sbn.isOngoing(), 1));
						
						Log.v(TAG, "New id returned: " + mUri.toString());

						if (mUri == null) {
							Log.d(TAG, "failure");
						}
					} catch (Exception e) {
						Log.e(TAG, e.getClass().getName());
						e.printStackTrace();
					}
				} else {
					try {
						if (preferences.getBoolean(
								"stack_on" + Integer.toString(extNumber), false))
							notifText.set(1, notifText.get(1) + "\n" + countCheck.getString(4));
						
						int count = getContentResolver().update(
								NotificationProvider.CONTENT_URI,
								createContentValue(sbn.getPackageName(), sbn
										.getId(), notifText, nTime,
										ticker != null ? ticker.toString() : null, sbn.isClearable(),
										sbn.isOngoing(), countCheck.getInt(10) + 1),
								NotifSQLiteHelper.COL_PNAME + " = ? AND "
										+ NotifSQLiteHelper.COL_NOTIF_ID
										+ " = ? ",
								new String[] { sbn.getPackageName(),
										Integer.toString(sbn.getId()) });
						Log.d(TAG, "Updated: " + Integer.toString(count));
					} catch (Exception e) {

						Log.e(TAG, e.getClass().getName());
						e.printStackTrace();
					}
				}
				countCheck.close();

			}
		}
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {

		Log.v("DashNotifier", "Removed: " + sbn.getPackageName() + ": "
				+ Integer.toString(sbn.getId()));
		try {
			int count = getContentResolver().delete(
					NotificationProvider.CONTENT_URI,
					NotifSQLiteHelper.COL_PNAME + " = ? AND "
							+ NotifSQLiteHelper.COL_NOTIF_ID + " = ? ",
					new String[] { sbn.getPackageName(),
							Integer.toString(sbn.getId()) });
			Log.v(TAG, "Deleted: " + Integer.toString(count));
		} catch (Exception e) {

			Log.e(TAG, e.getClass().getName());
			e.printStackTrace();
		}

	}

	private ContentValues createContentValue(String pName, int id,
			ArrayList<String> nText, String time, String ticker, boolean clear,
			boolean ongoing, int count) {
		ContentValues cvResult = new ContentValues();
		cvResult.put(NotifSQLiteHelper.COL_PNAME, pName);
		cvResult.put(NotifSQLiteHelper.COL_NOTIF_ID, id);
		cvResult.put(NotifSQLiteHelper.COL_TITLE, nText.get(0));
		cvResult.put(NotifSQLiteHelper.COL_TEXT, nText.get(1));

		if (nText.size() > 2)
			cvResult.put(NotifSQLiteHelper.COL_EXTRA, nText.get(2));
		else
			cvResult.put(NotifSQLiteHelper.COL_EXTRA, "");

		cvResult.put(NotifSQLiteHelper.COL_TIME, time);
		cvResult.put(NotifSQLiteHelper.COL_TICKER, ticker);
		cvResult.put(NotifSQLiteHelper.COL_CLEAR, clear ? 1 : 0);
		cvResult.put(NotifSQLiteHelper.COL_ONGOING, ongoing ? 1 : 0);
		cvResult.put(NotifSQLiteHelper.COL_COUNT, count);
		return cvResult;

	}

	private int extMatch(String packageName) {
		if (packageName.equals(preferences
				.getString("extapp1", "dummy.xx.name")))
			return 1;
		else if (packageName.equals(preferences.getString("extapp2",
				"dummy.xx.name"))) 
			return 2;
		else if (packageName.equals(preferences.getString("extapp3",
				"dummy.xx.name")))
			return 3;
		else if (packageName.equals(preferences.getString("extapp4",
				"dummy.xx.name")))
			return 4;
		else if (packageName.equals(preferences.getString("extapp5",
				"dummy.xx.name")))
			return 5;
		else if (packageName.equals(preferences.getString("extapp6",
				"dummy.xx.name")))
			return 6;
		else if (packageName.equals(preferences.getString("extapp7",
				"dummy.xx.name")))
			return 7;
		else
			return -1;
	}
	
	private boolean isDisplayed(String extNumber, boolean clearable){
		if (extNumber.equals("-1"))
			return false;
		else{
			if (clearable)
				return true;
			else if (preferences.getBoolean(
					"show_ongoing" + extNumber, false) && !clearable)
				return true;
			else
				return false;
		}
		
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
			// Log.d(TAG,Float.toString(event.values[0]));
			sensorValue = event.values[0];
		}

	}

	private void turnMeOn() {
		@SuppressWarnings("deprecation")
		PowerManager.WakeLock wl = pm.newWakeLock(
				PowerManager.SCREEN_DIM_WAKE_LOCK
						| PowerManager.ACQUIRE_CAUSES_WAKEUP, "DashNotifier");
		wl.acquire();
		wl.release();
	}
	
	@SuppressLint("UseSparseArrays")
	public static ArrayList<String> extractor(Notification notification){
		ArrayList<String> notifText = new ArrayList<String>();
	    RemoteViews views = notification.contentView;
	    @SuppressWarnings("rawtypes")
		Class secretClass = views.getClass();

	    try {
	        
	        Field outerFields[] = secretClass.getDeclaredFields();
	        for (int i = 0; i < outerFields.length; i++) {
	        	
	            if (!outerFields[i].getName().equals("mActions")) continue;

	            outerFields[i].setAccessible(true);

	            @SuppressWarnings("unchecked")
				ArrayList<Object> actions = (ArrayList<Object>) outerFields[i]
	                    .get(views);
	            for (Object action : actions) {
	            
	                Field innerFields[] = action.getClass().getDeclaredFields();
	            
	                Object value = null;
	                Integer type = null;
	                @SuppressWarnings("unused")
					Integer viewId = null;
	                for (Field field : innerFields) {
	            
	                    field.setAccessible(true);
	                    if (field.getName().equals("value")) {
	                        value = field.get(action);
	                    } else if (field.getName().equals("type")) {
	                        type = field.getInt(action);
	                    } else if (field.getName().equals("viewId")) {
	                        viewId = field.getInt(action);
	                    }
	                }

	            
	                if (type != null && (type == 9 || type == 10) && value != null){
	                	System.out.println("Type: " + Integer.toString(type) + " Value: " + value.toString());
	                	if ( !notifText.contains(value.toString()) )
	                		notifText.add(value.toString());
	                }
	                	
	            }
	        }
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return notifText;
	}

	

	

}
