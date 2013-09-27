package com.umang.dashnotifier;

import android.app.Activity;
import android.content.*;
import android.text.TextUtils;
import android.view.*;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import de.keyboardsurfer.android.widget.crouton.Configuration;

public abstract class AbstractSettings extends Activity {

	final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
	android.content.SharedPreferences.Editor editor;
	SharedPreferences preferences;
	boolean service_running;

	public AbstractSettings() {
		service_running = false;
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
			startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
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
		String s = (new ComponentName(this, DashNotificationListener.class)).flattenToString();
		String s1 = android.provider.Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
		if(!TextUtils.isEmpty(s1) && s1.contains(s))
			service_running = true;
	}
}
