package com.umang.dashnotifier;

import java.util.List;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.accessibility.AccessibilityManager;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import de.keyboardsurfer.android.widget.crouton.Configuration;

public abstract class AbstractSettings extends Activity {

	final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
	android.content.SharedPreferences.Editor editor;
	SharedPreferences preferences;
	boolean service_running;
	int apiVersion;
	

	public AbstractSettings() {
		service_running = false;
		apiVersion = android.os.Build.VERSION.SDK_INT;
	}

	protected abstract void onActivityResult(int i, int j, Intent intent);

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.action_menu, menu);
		return true;
	}

	protected void onDestroy() {
		Crouton.clearCroutonsForActivity(this);
		super.onDestroy();
	}

	public boolean onOptionsItemSelected(MenuItem menuitem) {
		switch (menuitem.getItemId()){
		case R.id.action_service_status:{
			if (apiVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2)
				startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
			else
				startActivity(new Intent("android.settings.ACCESSIBILITY_SETTINGS"));
			break;
		}
		}
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem menuitem = menu.findItem(R.id.action_service_status);
		if(service_running)
			menuitem.setVisible(false);
		else
			menuitem.setIcon(R.drawable.ic_action_service_bad);
		return true;
	}

	protected void onRestart() {
		super.onRestart();
		invalidateOptionsMenu();
	}

	protected void onResume() {
		super.onResume();
		Crouton.clearCroutonsForActivity(this);
		Configuration CONFIGURATION_INFINITE = new Configuration.Builder()
        .setDuration(Configuration.DURATION_INFINITE)
        .build();
		Crouton crouton = Crouton.makeText(this, R.string.notification_access_warn, Style.ALERT);
		crouton.setConfiguration(CONFIGURATION_INFINITE);
		if(!service_running)
			crouton.show();
	}

	protected void onStart() {
		super.onStart();
		
		if (apiVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2){
			String myComponentName = (new ComponentName(this, DashNotificationListener.class)).flattenToString();
			String notifListeners = android.provider.Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
			if(!TextUtils.isEmpty(notifListeners) && notifListeners.contains(myComponentName))
				service_running = true;
		}
		else{
			String myComponentName = (new ComponentName(this, DashNotificationListenerAcc.class)).flattenToShortString();
			AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
			List<AccessibilityServiceInfo> accList = accessibilityManager.getInstalledAccessibilityServiceList();
			
			for (AccessibilityServiceInfo service : accList) {
				System.out.println(service.getId() + "::" + myComponentName);
				if (service.getId().equals(myComponentName))
					service_running = true;
			}
		}
	}
}
