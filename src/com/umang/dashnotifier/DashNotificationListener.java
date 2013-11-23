package com.umang.dashnotifier;

import java.io.File;
import java.util.ArrayList;
import com.umang.dashnotifier.provider.NotifSQLiteHelper;
import com.umang.dashnotifier.provider.NotificationProvider;
import com.umang.dashnotifier.provider.NotificationStore;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class DashNotificationListener extends NotificationListenerService {

	private static final String TAG = "DashNotificationListener";

	ContentResolver resolver;
	String[] projection;
	ArrayList<String> notifText;
	SharedPreferences preferences;
	boolean screen_on_pref;
	PowerManager pm;
	Uri mUri;
	SharedPreferences.Editor editor;

	@Override
	public void onCreate() {
		super.onCreate();
		projection = NotificationStore.allColumns;
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
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
		// Log.v(TAG, Integer.toString(sbn.getNotification().icon));

		CharSequence ticker = sbn.getNotification().tickerText;
		
		int extNumber = extMatch(sbn.getPackageName());

		if (isDisplayed(Integer.toString(extNumber), sbn.isClearable())) {
			String nTime = DateFormat
					.format("hh:mm", sbn.getPostTime() * 1000L).toString();
			screen_on_pref = preferences.getBoolean(
					"turn_screen_on" + Integer.toString(extNumber), false);
			
			if (preferences.getBoolean("notification_icon_preference"+ Integer.toString(extNumber),false)){
				String iconFileName = getFilesDir() + "/"+sbn.getPackageName()+ Integer.toString(sbn.getNotification().icon) + ".png";
				File iconFromNotification = new File(iconFileName);
				if (!iconFromNotification.exists()){
					
					boolean stat = Commons.bitmapToFile(
							Commons.drawableToBitmap(getPackageManager().getDrawable(
									sbn.getPackageName(), sbn.getNotification().icon,
									null)), iconFileName);
					if (stat){
						editor.putString("iconExt"+Integer.toString(extNumber), iconFileName);
					    editor.commit();
					}
						
				}
				editor.putString("iconExt"+Integer.toString(extNumber), iconFileName);
			    editor.commit();
			}
			
			

			if (!pm.isScreenOn() && screen_on_pref)
				turnMeOn();

			/*
			 * notifText: Index 0 - notification title, 1 - notification text, 2
			 * - notification extra eg. with audio players it's generally play
			 * list position
			 */
			notifText = Commons.extractor(sbn.getNotification());
			if (notifText.size() == 1){
				ApplicationInfo content;
				try {
					content = getPackageManager().getApplicationInfo(sbn.getPackageName().toString(), 0);
					final String appName = getPackageManager().getApplicationLabel(content).toString();
					notifText.add(0, appName);
				} catch (NameNotFoundException e) {
					Log.e(TAG,"Error retrieving package name");
				}
				
			}

			Log.v(TAG, "In listener: " + notifText.toString());

			// 2 - For title and text
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
										ticker != null ? ticker.toString()
												: null, sbn.isClearable(), sbn
												.isOngoing(), 1));

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
						// ignore duplicate notifications from same app. Only
						// checks notification text to check duplicity.
						int newCount = countCheck.getString(4).equals(
								notifText.get(1)) ? countCheck.getInt(10)
								: countCheck.getInt(10) + 1;

						if (preferences
								.getBoolean(
										"stack_on"
												+ Integer.toString(extNumber),
										false))
							notifText.set(1, notifText.get(1) + "\n"
									+ countCheck.getString(4));

						int count = getContentResolver().update(
								NotificationProvider.CONTENT_URI,
								createContentValue(sbn.getPackageName(), sbn
										.getId(), notifText, nTime,
										ticker != null ? ticker.toString()
												: null, sbn.isClearable(), sbn
												.isOngoing(),
										// check on count to ignore duplicate
										newCount),
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
		int extNumber = extMatch(sbn.getPackageName());
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
			//restore from backup fr always_show extensions
			if (preferences.getBoolean("always_show"+ Integer.toString(extNumber), false)){
				if (!TextUtils.isEmpty(preferences.getString("iconExt_default_"+Integer.toString(extNumber), "")))
					editor.putString("iconExt"+Integer.toString(extNumber), preferences.getString("iconExt_default_"+Integer.toString(extNumber), ""));
				else{
					editor.remove("iconExt"+Integer.toString(extNumber));
					editor.putString("icon_preference"+Integer.toString(extNumber), preferences.getString("icon_preference_default_"+Integer.toString(extNumber), ""));
				}
					
				editor.commit();
			}
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
		else if (packageName.equals(preferences.getString("extapp8",
				"dummy.xx.name")))
			return 8;
		else if (packageName.equals(preferences.getString("extapp9",
				"dummy.xx.name")))
			return 9;
		else if (packageName.equals(preferences.getString("extapp10",
				"dummy.xx.name")))
			return 10;
		else if (packageName.equals(preferences.getString("extapp11",
				"dummy.xx.name")))
			return 11;
		else
			return -1;
	}

	private boolean isDisplayed(String extNumber, boolean clearable) {
		if (extNumber.equals("-1"))
			return false;
		else {
			if (clearable)
				return true;
			else if (preferences.getBoolean("show_ongoing" + extNumber, false)
					&& !clearable)
				return true;
			else
				return false;
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

	
}
