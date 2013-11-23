package com.umang.dashnotifier;


import com.google.android.apps.dashclock.configuration.AppChooserPreference;
import com.umang.dashnotifier.provider.NotifSQLiteHelper;
import com.umang.dashnotifier.provider.NotificationProvider;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
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
import android.preference.CheckBoxPreference;
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
	Preference iconPreference;
	
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
		prefText = getResources().getStringArray(R.array.entries_list_preference);
		prefValues = getResources().getStringArray(R.array.entryvalues_list_preference);
		preferences = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
	}
	
	@Override
	public void onDestroy() {
	    Crouton.clearCroutonsForActivity(getActivity());
	    super.onDestroy();
	  }
	
	@Override
	public void onResume(){
		super.onResume();
		Drawable icon;
		String packageName = preferences.getString("extapp"+extNumber, "dummy.xx.name"); 
		IconPicker ip = new IconPicker(getActivity(), this, extNumber);
		if (getPreferenceScreen().findPreference("icon_preference"+extNumber) != null)
			iconPreference = getPreferenceScreen().findPreference("icon_preference"+extNumber);
		
		//Disable Show ongoing setting for Android < 4.3
		if (apiVersion < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2){
			if (getPreferenceScreen().findPreference("show_ongoing"+extNumber) != null)
				getPreferenceScreen().removePreference(getPreferenceScreen().findPreference("show_ongoing"+extNumber));
			CheckBoxPreference unlock_cbp = (CheckBoxPreference) getPreferenceScreen().findPreference("clear_on_unlock"+extNumber);
			unlock_cbp.setSummaryOff(R.string.unlock_off_api17);
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
				
				if (getPreferenceScreen().findPreference("icon_preference"+extNumber) != null){
					getPreferenceScreen().findPreference("icon_preference"+extNumber).
					setOnPreferenceClickListener(ip);
					String iconPath = preferences.getString("iconExt"+extNumber, "");
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
				}
				
				if (preferences.getBoolean("notification_icon_preference"+extNumber, false)){
					if (!preferences.getBoolean("always_show"+extNumber,false) && (getPreferenceScreen().findPreference("icon_preference"+extNumber)!= null))
						getPreferenceScreen().removePreference(getPreferenceScreen().findPreference("icon_preference"+extNumber));
				}
				
				getPreferenceScreen().findPreference("notification_icon_preference"+extNumber).setOnPreferenceClickListener
				(new Preference.OnPreferenceClickListener(){
					@Override
					public boolean onPreferenceClick(Preference preference) {
						boolean alwaysShow = preferences.getBoolean("always_show"+extNumber, false); 
						boolean showNotificationIcon = preferences.getBoolean(preference.getKey(), false);
						if (showNotificationIcon && !alwaysShow && (getPreferenceScreen().findPreference("icon_preference"+extNumber)!= null)){
							getPreferenceScreen().removePreference(getPreferenceScreen().findPreference("icon_preference"+extNumber));
						}
						else if (!showNotificationIcon )
							getPreferenceScreen().addPreference(iconPreference);
						
						return true;
					}
				});
				
				
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
				
				getPreferenceScreen().findPreference("always_show"+extNumber).setOnPreferenceClickListener
				(new Preference.OnPreferenceClickListener(){
					@Override
					public boolean onPreferenceClick(Preference preference) {
						boolean alwaysShow = preferences.getBoolean(preference.getKey(), false); 
						boolean showNotificationIcon = preferences.getBoolean("notification_icon_preference"+extNumber, false);
						
						if (showNotificationIcon && alwaysShow){
							Crouton.makeText(getActivity(), R.string.notif_icon_warn_always, Style.INFO).show();
							getPreferenceScreen().addPreference(iconPreference);
						}
							
						
						else if (showNotificationIcon && !alwaysShow && (getPreferenceScreen().findPreference("icon_preference"+extNumber)!= null))
							getPreferenceScreen().removePreference(getPreferenceScreen().findPreference("icon_preference"+extNumber));
							
						return true;
					}
				});
				
				if (TextUtils.isEmpty(preferences.getString("ext_title_preference"+extNumber, "app_count"))){
					preferences.edit().putString("ext_title_preference"+extNumber, getString(R.string.default_title_pref)).commit();
				}
					
					
				
				getPreferenceScreen().findPreference("ext_title_preference"+extNumber).setSummary
				(prefText[findPosition(prefValues,preferences.getString("ext_title_preference"+extNumber, "app_count"))]);
				
				if (preferences.getString("ext_title_preference"+extNumber, "app_count").contains("custom"))
					getPreferenceScreen().findPreference("text_preference"+extNumber).setEnabled(true);
				
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
		//return index of notif_nocount. Added for missing string value in v0.46 in Italiano
		return 4;
	}

}
