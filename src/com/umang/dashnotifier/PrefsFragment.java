package com.umang.dashnotifier;


import com.google.android.apps.dashclock.configuration.AppChooserPreference;
import com.umang.dashnotifier.provider.NotifSQLiteHelper;
import com.umang.dashnotifier.provider.NotificationProvider;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

public class PrefsFragment extends PreferenceFragment implements IconPicker.OnIconPickListener {

	SharedPreferences preferences;
	private String extNumber;
	String[] prefText;
	String[] prefValues;
	int preferencesResId;
	private static final String TAG = "PrefsFragment";
	int apiVersion;
	
	public PrefsFragment(){
		
	}
	
	
	public PrefsFragment(String number){
		extNumber = number;
		apiVersion = android.os.Build.VERSION.SDK_INT;
	}
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		preferencesResId = getResources()
				.getIdentifier("dashnotifiersettings"+ extNumber, "xml",
						getActivity().getPackageName());
		// Load the preferences from an XML resource
		addPreferencesFromResource(preferencesResId);
		preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		prefText = getResources().getStringArray(R.array.entries_list_preference);
		prefValues = getResources().getStringArray(R.array.entryvalues_list_preference);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		Drawable icon;
		String packageName = preferences.getString("extapp"+extNumber, "dummy.xx.name"); 
		IconPicker ip = new IconPicker(getActivity(), this, extNumber);
		
		//Disable Show ongoing setting for Android < 4.3
		if (apiVersion < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2){
			if (getPreferenceScreen().findPreference("show_ongoing"+extNumber) != null)
				getPreferenceScreen().removePreference(getPreferenceScreen().findPreference("show_ongoing"+extNumber));
		}
		
		if (!packageName.equals("dummy.xx.name")){
			PackageManager pm = getActivity().getPackageManager();
			
			try {
				
				Resources r = getResources();
				ApplicationInfo content = pm.getApplicationInfo(packageName, 0);
				final String appName = pm.getApplicationLabel(content).toString();
				getPreferenceScreen().findPreference("extapp"+extNumber).setTitle(appName);
				getPreferenceScreen().findPreference("extapp"+extNumber).setIcon(
						pm.getApplicationIcon(packageName));
				String iconPath = preferences.getString("iconExt"+extNumber, "");
				CharSequence intentSummary = AppChooserPreference.getDisplayValue(getActivity(), preferences.getString
						("click_intent"+extNumber,appName ));
				getPreferenceScreen().findPreference("click_intent"+extNumber).setSummary
				(TextUtils.isEmpty(intentSummary) || intentSummary.equals(r.getString(R.string.pref_shortcut_default)) ?
						appName : intentSummary);
				getPreferenceScreen().findPreference("click_intent"+extNumber).setOnPreferenceChangeListener
				( new Preference.OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(
							Preference preference, Object newValue) {
						CharSequence intentSummary = AppChooserPreference.getDisplayValue(getActivity(), newValue.toString());
						getPreferenceScreen().findPreference("click_intent"+extNumber).setSummary
						(TextUtils.isEmpty(intentSummary) || intentSummary.equals(getResources().getString(R.string.pref_shortcut_default)) ?
								appName : intentSummary);
						return true;
					}
					
				});
				
				
				getPreferenceScreen().findPreference("ext_title_preference"+extNumber).setSummary
				(prefText[findPosition(prefValues,preferences.getString("ext_title_preference"+extNumber, "app_count"))]);
				
				if (preferences.getString("ext_title_preference"+extNumber, "app_count").contains("custom"))
					getPreferenceScreen().findPreference("text_preference"+extNumber).setEnabled(true);
				
				try {
					
					if(!TextUtils.isEmpty(iconPath))
						icon = Drawable.createFromPath(iconPath);
					else{
						int iconId = r.getIdentifier(
								preferences.getString("icon_preference" + extNumber,
										"dummy"), "drawable", getActivity().getPackageName());
						icon = getResources().getDrawable(iconId);
					}
					icon.setColorFilter( Color.DKGRAY, Mode.MULTIPLY );
					getPreferenceScreen().findPreference("icon_preference"+ extNumber).setIcon(icon);
				} catch (NotFoundException e) {
					Log.w("DashNotifier"+extNumber, "No icon set");
				} 
				
				getPreferenceScreen().findPreference("icon_preference"+extNumber).
				setOnPreferenceClickListener(ip);
				
				if (getPreferenceScreen().findPreference("show_ongoing"+extNumber) != null){
					getPreferenceScreen().findPreference("show_ongoing"+extNumber).setOnPreferenceClickListener
					(new Preference.OnPreferenceClickListener(){
						@Override
						public boolean onPreferenceClick(Preference preference) {
							String packageName = preferences.getString("extapp"+extNumber, "dummy.xx.name"); 
							int count = getActivity().getContentResolver().delete(
									NotificationProvider.CONTENT_URI,
									NotifSQLiteHelper.COL_PNAME + " = ? ",
									new String[] { packageName });
							Log.v(TAG,"Deleted: " + Integer.toString(count));
							
							return true;
						}
					});
				}
				
				
				getPreferenceScreen().findPreference("ext_title_preference"+extNumber).setOnPreferenceChangeListener
				( new Preference.OnPreferenceChangeListener() {
					
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						preference.setSummary(prefText[findPosition(prefValues,(String)newValue)]);
						if ( newValue.toString().contains("custom"))
							getPreferenceScreen().findPreference("text_preference"+extNumber).setEnabled(true);
						else
							getPreferenceScreen().findPreference("text_preference"+extNumber).setEnabled(false);
						return true;
					}
				});
					
						
			} catch (NameNotFoundException e) {
				Log.d(TAG, e.getStackTrace().toString());
				e.printStackTrace();
			}
		}
		
		getPreferenceScreen().findPreference("about"+extNumber).setOnPreferenceClickListener
		(new Preference.OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference preference) {
				HelpUtils.showAboutDialog(
                        getActivity());
				
				return true;
			}
		});
		
		
	}

	@Override
	public void iconPicked() {
		int i = getResources().getIdentifier(preferences.getString("icon_preference"+ extNumber, "dummy"), "drawable", getActivity().getPackageName());
		Drawable drawable = getResources().getDrawable(i);
		if(drawable != null) {
			drawable.setColorFilter(Color.DKGRAY, android.graphics.PorterDuff.Mode.MULTIPLY);
			getPreferenceScreen().findPreference("icon_preference"+ extNumber).setIcon(drawable);
		}
	}
	
	public int findPosition(String[] array, String value){
		for (int i = 0; i < array.length; i++){
			if (array[i].equals(value))
				return i;
		}
		return -1;
	}

}
