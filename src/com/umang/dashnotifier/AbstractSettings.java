package com.umang.dashnotifier;

import java.util.List;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import de.keyboardsurfer.android.widget.crouton.Configuration;

public abstract class AbstractSettings extends Activity {

	final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
	android.content.SharedPreferences.Editor editor;
	static SharedPreferences preferences;
	boolean service_running;
	int apiVersion;
	
	public AbstractSettings() {
		service_running = false;
		apiVersion = android.os.Build.VERSION.SDK_INT;
		
	}

	protected abstract void onActivityResult(int i, int j, Intent intent);

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.action_menu, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		Crouton.clearCroutonsForActivity(this);
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuitem) {
		switch (menuitem.getItemId()){
		case R.id.action_service_status:{
			if (apiVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2)
				startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
			else
				startActivity(new Intent("android.settings.ACCESSIBILITY_SETTINGS"));
			break;
		}
		case R.id.action_settings:{
			showDialog();
			break;
		}
		}
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem menuitem = menu.findItem(R.id.action_service_status);
		if(service_running)
			menuitem.setVisible(false);
		else
			menuitem.setIcon(R.drawable.ic_action_service_bad);
		return true;
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		invalidateOptionsMenu();
	}

	@Override
	protected void onResume() {
		super.onResume();
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Crouton.clearCroutonsForActivity(this);
		Configuration CONFIGURATION_INFINITE = new Configuration.Builder()
        .setDuration(Configuration.DURATION_INFINITE)
        .build();
		Crouton crouton = Crouton.makeText(this, R.string.notification_access_warn, Style.ALERT);
		crouton.setConfiguration(CONFIGURATION_INFINITE);
		if(!service_running)
			crouton.show();
	}

	@Override
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
			List<AccessibilityServiceInfo> accList = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
			
			for (AccessibilityServiceInfo service : accList) {
				
				if (service.getId().equals(myComponentName))
					service_running = true;
			}
		}
	}
	
	void showDialog() {
	    DialogFragment newFragment = ExtensionCountDialog.newInstance(
	            R.string.ext_count_dialog_title);
	    newFragment.show(getFragmentManager(), "dialog");
	}

	public void doPositiveClick() {
	    Toast.makeText(this, R.string.ext_onoff_toast, Toast.LENGTH_LONG).show();
	}

	
	public static class ExtensionCountDialog extends DialogFragment {
		
		public static ExtensionCountDialog newInstance(int title) {
			ExtensionCountDialog frag = new ExtensionCountDialog();
	        Bundle args = new Bundle();
	        args.putInt("title", title);
	        frag.setArguments(args);
	        return frag;
	    }
		
		@Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        int title = getArguments().getInt("title");
	        ListView list = new ListView(getActivity());
	        final ExtensionAdapter adapter = new ExtensionAdapter();
	        list.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					
					if (position == 0)
						return;
					
					String ext = "";
					CheckedTextView ctv = (CheckedTextView) view.findViewById(R.id.ext_name);
					
					if (position != 0){
						ext = Integer.toString(position+1);
					}
					
					if ((Boolean)ctv.getTag()){
						getActivity().getPackageManager().setComponentEnabledSetting(new ComponentName(getActivity(),getActivity().getPackageName()+".DashNotifierExtension"+ext),PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
						ctv.setCheckMarkDrawable(R.drawable.btn_check_off_holo_light);
						ctv.setTag(false);
					}
					else{
						getActivity().getPackageManager().setComponentEnabledSetting(new ComponentName(getActivity(),getActivity().getPackageName()+".DashNotifierExtension"+ext),PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
						ctv.setCheckMarkDrawable(R.drawable.btn_check_on_holo_light);
						ctv.setTag(true);
					}
					ctv.setChecked(true);
					
				}
			});
					
			
			list.setAdapter(adapter);
	        return new AlertDialog.Builder(getActivity())
	                .setIcon(R.drawable.ic_launcher)
	                .setTitle(title)
	                .setPositiveButton(android.R.string.ok,
	                    new DialogInterface.OnClickListener() {
	                        public void onClick(DialogInterface dialog, int whichButton) {
	                            ((AbstractSettings)getActivity()).doPositiveClick();
	                        }
	                    }
	                )
	                .setView(list)
	                .create();
	    }
		
		class ExtensionAdapter extends BaseAdapter {

			String[] labels;
			String[] defaultStatus;
			
			public ExtensionAdapter() {
				labels = getResources().getStringArray(R.array.extension_names);
				defaultStatus = getResources().getStringArray(R.array.default_extension_status);
			}
			
			@Override
			public int getCount() {
				return labels.length;
			}

			@Override
			public Object getItem(int position) {
				return labels[position];
			}

			@Override
			public long getItemId(int position) {
				return 0;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				String ext = "";
				String prefExt = Integer.toString(position+1);
				boolean isEnabled = false;
				int status = -1;
				if (convertView == null) {
					convertView = View.inflate(getActivity(), R.layout.count_dialog_row, null);
				}
				if (position != 0){
					ext = Integer.toString(position+1);
				}
				
				CheckedTextView ctv = (CheckedTextView)convertView.findViewById(R.id.ext_name);
				TextView tv = (TextView) convertView.findViewById(R.id.app_name);
				ImageView iv = (ImageView)convertView.findViewById(R.id.icon);
				status = getActivity().getPackageManager().getComponentEnabledSetting(new ComponentName(getActivity(),getActivity().getPackageName()+".DashNotifierExtension"+ext));
				
				if (status == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
					isEnabled = Boolean.parseBoolean(defaultStatus[position]);
				else if (status == PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
					isEnabled = false;
				else if (status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
					isEnabled = true;
				
				try {
					int bound = getActivity().getResources().getDimensionPixelSize(
							R.dimen.shortcut_picker_left_padding);
					int padding = getActivity().getResources().getDimensionPixelSize(
							R.dimen.ext_icon_padding);
					
					Drawable icon = getActivity().getPackageManager().getApplicationIcon(preferences.getString("extapp"+prefExt,""));
					icon.setBounds(0,0,bound,bound);
					iv.setPadding(padding, padding, padding, padding);
					iv.setImageDrawable(icon);
					
					tv.setText(getActivity().getPackageManager().getApplicationLabel(getActivity().getPackageManager().getApplicationInfo(preferences.getString("extapp"+prefExt,""), 0)));
					
				} catch (NameNotFoundException e) {
					tv.setText("");
					iv.setImageResource(R.drawable.ic_null);
				}
				
				if (isEnabled)
					ctv.setCheckMarkDrawable(R.drawable.btn_check_on_holo_light);
				else
					ctv.setCheckMarkDrawable(R.drawable.btn_check_off_holo_light);
				
				ctv.setText(labels[position]);	
				ctv.setChecked(true);
				ctv.setTag(isEnabled);
				
				//Don't allow disabling extension 1 to avoid disabling all extensions, from which there is no comeback
				if (position == 0){
					convertView.setEnabled(false);
					convertView.setClickable(false);
				}
				
				return convertView;
			}
			
		}
	}
}
