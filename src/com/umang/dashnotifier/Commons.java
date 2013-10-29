package com.umang.dashnotifier;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;
import com.google.android.apps.dashclock.configuration.AppChooserPreference;
import com.umang.dashnotifier.provider.NotifSQLiteHelper;
import com.umang.dashnotifier.provider.NotificationProvider;
import com.umang.dashnotifier.provider.NotificationStore;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.RemoteViews;

public class Commons {
	static SharedPreferences mPreferences;
	static PackageManager mPm;
	static String[] mPrefValues;
	static int apiVersion = android.os.Build.VERSION.SDK_INT;;

	public static ExtensionData publishOrPerish(DashClockExtension mContext,
			String extNumber, SharedPreferences preferences, PackageManager pm,
			String[] prefValues) {
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
				String title = makeTitle(result, packageName, extNumber);
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
				} finally {
					clickIntent = AppChooserPreference.getIntentValue(
							preferences.getString("click_intent" + extNumber,
									""), clickIntent);
				}

				result.close();

				extensionData = new ExtensionData().visible(true).status("0")
						.expandedTitle(title)
						.icon(iconId == 0 ? R.drawable.ic_extension : iconId)
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
			String title = makeTitle(result, packageName, extNumber);
			String status = "";
			int iconId = mContext.getResources().getIdentifier(
					preferences.getString("icon_preference" + extNumber,
							"dummy"), "drawable", mContext.getPackageName());

			Intent clickIntent = new Intent();

			if (apiVersion < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
				clickIntent.putExtra("packageName", packageName);
				clickIntent.putExtra("extNumber", extNumber);
				clickIntent.setClass(mContext, ClickIntentActivity.class);

			} else {
				try {
					clickIntent = pm.getLaunchIntentForPackage(packageName)
							.addCategory(Intent.CATEGORY_DEFAULT);
				} catch (NullPointerException npe) {
					clickIntent.setAction(Intent.ACTION_MAIN);
					clickIntent.addCategory(Intent.CATEGORY_HOME);
					clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				} finally {
					clickIntent = AppChooserPreference.getIntentValue(
							preferences.getString("click_intent" + extNumber,
									""), clickIntent);
				}
			}

			if (preferences.getBoolean("content_on" + extNumber, false)) {
				if (mPreferences.getString("ext_title_preference" + extNumber,
						"app_count").contains("notif")) {
					if (mPreferences.getBoolean("number_on" + extNumber, true))
						body = notifText + " " + notifExtra;
					else
						body = notifText;
				} else {
					if (mPreferences.getBoolean("number_on" + extNumber, true))
						body = notifTitle + "\n" + notifText + " " + notifExtra;
					else
						body = notifTitle + "\n" + notifText;
				}

			} else
				body = notifTitle;

			status = count;
			if (result.getInt(8) == 0)
				status = title;

			result.close();
			extensionData = new ExtensionData().visible(true).status(status)
					.expandedTitle(title)
					.icon(iconId == 0 ? R.drawable.ic_extension : iconId)
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

	// DashClockExtension mContext, String extNumber, SharedPreferences
	// preferences, PackageManager pm, String[] prefValues
	private static String makeTitle(Cursor result, String packageName,
			String extNumber) {
		String title = "";
		String prefValue = mPreferences.getString("ext_title_preference"
				+ extNumber, "app_count");
		// App name, no count
		if (prefValue.equals(mPrefValues[0])) {
			try {
				title = mPm.getApplicationLabel(
						mPm.getApplicationInfo(packageName, 0)).toString();
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		// App name with count
		else if (prefValue.equals(mPrefValues[1])) {
			int count = result.getCount() == 0 ? 0 : result.getInt(10);
			try {
				title = mPm.getApplicationLabel(
						mPm.getApplicationInfo(packageName, 0)).toString()
						+ " (" + Integer.toString(count) + ")";
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		// Custom, no count
		else if (prefValue.equals(mPrefValues[2])) {
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
		// Custom with count
		else if (prefValue.equals(mPrefValues[3])) {
			int count = result.getCount() == 0 ? 0 : result.getInt(10);
			try {
				String temp = mPreferences.getString("text_preference"
						+ extNumber, "");
				if (TextUtils.isEmpty(temp))
					title = mPm.getApplicationLabel(
							mPm.getApplicationInfo(packageName, 0)).toString()
							+ " (" + Integer.toString(count) + ")";
				else
					title = temp + " (" + Integer.toString(count) + ")";

			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		// Notification title, no count
		else if (prefValue.equals(mPrefValues[4])) {
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

		// Notification title with count
		else if (prefValue.equals(mPrefValues[5])) {
			int count = result.getCount() == 0 ? 0 : result.getInt(10);
			try {
				title = result.getString(3) + " (" + Integer.toString(count)
						+ ")";
			} catch (CursorIndexOutOfBoundsException e) {
				try {
					title = mPm.getApplicationLabel(
							mPm.getApplicationInfo(packageName, 0)).toString()
							+ " (" + Integer.toString(count) + ")";
				} catch (NameNotFoundException e1) {
					e1.printStackTrace();
				}
			}

		}

		result.moveToFirst();
		return title;

	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}

	public static boolean bitmapToFile(Bitmap bmp, String fileName) {
		try {
			FileOutputStream out = new FileOutputStream(fileName);
			bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.close();
			File iconFile = new File(fileName);
			iconFile.setReadable(true, false);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@SuppressLint("UseSparseArrays")
	public static ArrayList<String> extractor(Notification notification) {
		ArrayList<String> notifText = new ArrayList<String>();
		RemoteViews views = notification.contentView;
		@SuppressWarnings("rawtypes")
		Class secretClass = views.getClass();

		try {

			Field outerFields[] = secretClass.getDeclaredFields();
			for (int i = 0; i < outerFields.length; i++) {

				if (!outerFields[i].getName().equals("mActions"))
					continue;

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

					if (type != null && (type == 9 || type == 10)
							&& value != null) {
						// System.out.println("Type: " + Integer.toString(type)
						// + " Value: " + value.toString());
						if (!notifText.contains(value.toString()))
							notifText.add(value.toString());
					}

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return notifText;
	}

	@SuppressLint("WorldReadableFiles")
	public static void saveFileSetPreference(int requestCode, int resultCode,
			Intent imageReturnedIntent, Context mContext, String ext,
			SharedPreferences.Editor editor) {

		switch (requestCode) {
		case IconPicker.REQUEST_PICK_GALLERY:
			if (resultCode == Activity.RESULT_OK) {
				String iconFileName = "icon_" + ext + ".png";
				Uri selectedImage = imageReturnedIntent.getData();
				InputStream imageStream;
				try {
					imageStream = mContext.getContentResolver()
							.openInputStream(selectedImage);
					System.out.println(selectedImage.toString());
					@SuppressWarnings("deprecation")
					OutputStream stream = new BufferedOutputStream(
							mContext.openFileOutput(iconFileName,
									Context.MODE_WORLD_READABLE));
					int bufferSize = 1024;
					byte[] buffer = new byte[bufferSize];
					int len = 0;
					while ((len = imageStream.read(buffer)) != -1) {
						stream.write(buffer, 0, len);
					}
					if (stream != null) {
						stream.close();
						editor.putString("iconExt" + ext, mContext
								.getFilesDir().toString()
								+ "/icon_"
								+ ext
								+ ".png");
						editor.putString("iconExt_default_" + ext, mContext
								.getFilesDir().toString()
								+ "/icon_"
								+ ext
								+ ".png");
						editor.commit();
					}

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			break;
		case IconPicker.REQUEST_PICK_ICON_PACK:
			
			if (resultCode == Activity.RESULT_OK) {
				String iconFileName = mContext.getFilesDir().toString()
						+ "/icon_" + ext + ".png";
				Bitmap iconBitmap = null;
				if (imageReturnedIntent
						.hasExtra("android.intent.extra.shortcut.ICON_RESOURCE"))
					iconBitmap = (Bitmap) imageReturnedIntent.getExtras().get(
							"android.intent.extra.shortcut.ICON_RESOURCE");
				else if (imageReturnedIntent.hasExtra("icon"))
					iconBitmap = (Bitmap) imageReturnedIntent.getExtras().get(
							"icon");
				if (iconBitmap != null) {
					boolean stat = Commons.bitmapToFile(iconBitmap,
							iconFileName);
					if (stat) {
						editor.putString("iconExt" + ext, iconFileName);
						editor.putString("iconExt_default_" + ext, iconFileName);
						editor.commit();
					}
				}
			}
			break;

		}
	}

}
