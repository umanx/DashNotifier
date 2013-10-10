package com.umang.dashnotifier;

import java.io.File;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;
import com.google.android.apps.dashclock.configuration.AppChooserPreference;
import com.umang.dashnotifier.provider.NotifSQLiteHelper;
import com.umang.dashnotifier.provider.NotificationProvider;
import com.umang.dashnotifier.provider.NotificationStore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.text.TextUtils;

public class Commons {
	static SharedPreferences mPreferences;
	static PackageManager mPm;
	static String[] mPrefValues;

	
	public static ExtensionData publishOrPerish(DashClockExtension mContext,
			String extNumber, SharedPreferences preferences, PackageManager pm, String[] prefValues) {
		mPreferences = preferences;
		mPm = pm;
		mPrefValues = prefValues;
		String packageName = preferences.getString("extapp" + extNumber,
				"dummy.xx.name");
		ExtensionData extensionData;
		Cursor result = mContext.getContentResolver().query(
				NotificationProvider.CONTENT_URI, NotificationStore.allColumns,
				NotifSQLiteHelper.COL_PNAME + " = ? ",
				new String[] { packageName }, null);
		result.moveToFirst();
		if (result.getCount() == 0) {
			if (preferences.getBoolean("always_show" + extNumber, false)) {
				String title = makeTitle(result, packageName, extNumber );
				int iconId = mContext.getResources()
						.getIdentifier(
								preferences.getString("icon_preference"
										+ extNumber, "dummy"), "drawable",
								mContext.getPackageName());
				
				Intent clickIntent = new Intent();
				try {
					clickIntent = pm.getLaunchIntentForPackage(packageName)
							.addCategory(Intent.CATEGORY_DEFAULT);
				} catch (NullPointerException npe) {
					clickIntent.setAction(Intent.ACTION_MAIN);
					clickIntent.addCategory(Intent.CATEGORY_HOME);
					clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				}finally {
					clickIntent = AppChooserPreference.getIntentValue(preferences.getString
						("click_intent"+extNumber,"" ), clickIntent);
		        }
				result.close();

				extensionData = new ExtensionData().visible(true).status("0")
						.expandedTitle(title)
						.icon(iconId == 0 ? R.drawable.ic_launcher : iconId)
						.clickIntent(clickIntent);

				File iconFile = new File(preferences.getString("iconExt"
						+ extNumber, ""));
				if (iconFile.exists()) {
					Uri uri1 = Uri.fromFile(iconFile);
					extensionData.iconUri(uri1);
				}
				return extensionData;

			} else
				result.close();
			return null;

		} else {

			String body = null;
			String count = Integer.toString(result.getInt(10));
			String notifTitle = result.getString(3);
			String notifText = result.getString(4);
			String notifExtra = result.getString(5);
			String title = makeTitle(result, packageName, extNumber );
			//String expTitle = "";
			String status = "";
			int iconId = mContext.getResources().getIdentifier(
					preferences.getString("icon_preference" + extNumber,
							"dummy"), "drawable", mContext.getPackageName());

			Intent clickIntent = new Intent();
			try {
				clickIntent = pm.getLaunchIntentForPackage(packageName)
						.addCategory(Intent.CATEGORY_DEFAULT);
			} catch (NullPointerException npe) {
				clickIntent.setAction(Intent.ACTION_MAIN);
				clickIntent.addCategory(Intent.CATEGORY_HOME);
				clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			}finally {
					clickIntent = AppChooserPreference.getIntentValue(preferences.getString
						("click_intent"+extNumber,"" ), clickIntent);
		    }
			if (preferences.getBoolean("content_on" + extNumber, false)){
				if (mPreferences.getString("ext_title_preference"
						+ extNumber, "app_count").contains("notif"))
					body = notifText + " " + notifExtra;
				else
					body = notifTitle + "\n" + notifText + " " + notifExtra;
			}
			else
				body = notifTitle;

			status = count;
			if (result.getInt(8) == 0) 
				status = title;

			result.close();
			extensionData = new ExtensionData().visible(true).status(status)
					.expandedTitle(title)
					.icon(iconId == 0 ? R.drawable.ic_launcher : iconId)
					.expandedBody(body).clickIntent(clickIntent);

			File iconFile = new File(preferences.getString("iconExt"
					+ extNumber, ""));
			if (iconFile.exists()) {
				Uri uri1 = Uri.fromFile(iconFile);
				extensionData.iconUri(uri1);
			}
			return extensionData;

		}

	}
//DashClockExtension mContext, 	String extNumber, SharedPreferences preferences, PackageManager pm, String[] prefValues
	private static String makeTitle(Cursor result, String packageName, String extNumber ){
		String title = "";
		String prefValue = mPreferences.getString("ext_title_preference"
				+ extNumber, "app_count");
//		App name, no count
		if (prefValue.equals(mPrefValues[0])){
			try {
				title = mPm.getApplicationLabel(
						mPm.getApplicationInfo(packageName, 0))
						.toString();
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
//		App name with count
		else if (prefValue.equals(mPrefValues[1])){
			int count = result.getCount() == 0 ? 0 : result.getInt(10);
			try {
				title = mPm.getApplicationLabel(
						mPm.getApplicationInfo(packageName, 0))
						.toString() + " ("+Integer.toString(count) +")";
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
//		Custom, no count
		else if (prefValue.equals(mPrefValues[2])){
			try {
				String temp = mPreferences.getString("text_preference"
						+ extNumber, "");
				if (TextUtils.isEmpty(temp))
					title = mPm.getApplicationLabel(
							mPm.getApplicationInfo(packageName, 0)).toString();
				else
					title = temp;

			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
//		Custom with count
		else if (prefValue.equals(mPrefValues[3])){
			int count = result.getCount() == 0 ? 0 : result.getInt(10);
			try {
				String temp = mPreferences.getString("text_preference"
						+ extNumber, "");
				if (TextUtils.isEmpty(temp))
					title = mPm.getApplicationLabel(
							mPm.getApplicationInfo(packageName, 0)).toString() + " ("+Integer.toString(count) +")";
				else
					title = temp + " ("+Integer.toString(count) +")";

			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
//		Notification title, no count
		else if (prefValue.equals(mPrefValues[4])){
			try {
				title = result.getString(3);
			} catch (CursorIndexOutOfBoundsException e) {
				try {
					title = mPm.getApplicationLabel(
							mPm.getApplicationInfo(packageName, 0)).toString();
				} catch (NameNotFoundException e1) {
					e1.printStackTrace();
				}
			}
		}
		
//		Notification title with count
		else if (prefValue.equals(mPrefValues[5])){
			int count = result.getCount() == 0 ? 0 : result.getInt(10);
			try {
				title = result.getString(3) + " ("+Integer.toString(count) +")";
			} catch (CursorIndexOutOfBoundsException e) {
				try {
					title = mPm.getApplicationLabel(
							mPm.getApplicationInfo(packageName, 0)).toString() + " ("+Integer.toString(count) +")";
				} catch (NameNotFoundException e1) {
					e1.printStackTrace();
				}
			}
			
		}
		
		result.moveToFirst();
		return title;
		
	}
}

