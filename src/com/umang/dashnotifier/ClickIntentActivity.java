package com.umang.dashnotifier;

import com.google.android.apps.dashclock.configuration.AppChooserPreference;
import com.umang.dashnotifier.provider.NotifSQLiteHelper;
import com.umang.dashnotifier.provider.NotificationProvider;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;


public class ClickIntentActivity extends Activity {

	SharedPreferences preferences;
	SharedPreferences.Editor editor;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		String packageName = getIntent().getStringExtra("packageName");
		String extNumber = getIntent().getStringExtra("extNumber");
		Intent clickIntent = new Intent();
		try {
			int count = getContentResolver().delete(
					NotificationProvider.CONTENT_URI,
					NotifSQLiteHelper.COL_PNAME + " = ? AND "
							+ NotifSQLiteHelper.COL_NOTIF_ID + " = ? ",
					new String[] { packageName,
							Integer.toString(1) });
			Log.v("ClickIntentActivity", "Deleted: " + Integer.toString(count));
			//restore from backup fr always_show extensions
			if (preferences.getBoolean("always_show"+ extNumber, false)){
				if (!TextUtils.isEmpty(preferences.getString("iconExt_default_"+extNumber, "")))
					editor.putString("iconExt"+extNumber, preferences.getString("iconExt_default_"+extNumber, ""));
				else{
					editor.remove("iconExt"+extNumber);
					editor.putString("icon_preference"+extNumber, preferences.getString("icon_preference_default_"+extNumber, ""));
				}
					
				editor.commit();
			}
			try {
				clickIntent = getPackageManager().getLaunchIntentForPackage(packageName)
						.addCategory(Intent.CATEGORY_DEFAULT);
			} catch (NullPointerException npe) {
				clickIntent.setAction(Intent.ACTION_MAIN);
				clickIntent.addCategory(Intent.CATEGORY_HOME);
				clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			} finally {
				clickIntent = AppChooserPreference.getIntentValue(
						preferences.getString("click_intent" + extNumber, ""),
						clickIntent);
			}
			startActivity(clickIntent);
			finish();
		} catch (Exception e) {

			Log.e("ClickIntentActivity", e.getClass().getName());
			e.printStackTrace();
		}
		
	}

	

}
