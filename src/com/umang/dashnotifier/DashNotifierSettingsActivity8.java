package com.umang.dashnotifier;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

public class DashNotifierSettingsActivity8 extends AbstractSettings {
	static final String ext = "8";
	PrefsFragment mPrefsFragment;
	SharedPreferences preferences;
	SharedPreferences.Editor editor;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		editor = preferences.edit();
		// Display the fragment as the main content.
		FragmentManager mFragmentManager = getFragmentManager();
		mPrefsFragment = new PrefsFragment(ext);
	    mFragmentManager.beginTransaction().replace(android.R.id.content, mPrefsFragment,"pref").commit();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	        finish();
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
		Commons.saveFileSetPreference(requestCode, resultCode,
				imageReturnedIntent, this, ext, editor);
	}
}
