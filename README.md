DashNotifier
============
All in one extension for DashClock widget for Android 4.2+

Instruction for building (for first timers):
<ol>
<li>Setup Android SDK - http://developer.android.com/sdk/index.html
<li>Clone DashNotifier or download DashNotifier Source code - https://github.com/umanx/DashNotifier/archive/master.zip
<li>Extract the zip
<li>Import the project in Eclipse. File->New->Project->Android Project from Existing Code
<li>Download <a href="https://github.com/keyboardsurfer/Crouton">Crouton Library</a> jar from <a href="http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22de.keyboardsurfer.android.widget%22Maven">Maven</a> and put it in libs folder inside DashNotifier's folder
<li>You are now all set. Build and run the project from Eclipse.
</ol>

<ul>
<li>To compile this for lower android versions, change the <i>android:minSdkVersion</i> in AndroidManifest.xml file to what you want (15 for Android 4.0.3, 4.1 is 16 and so on) and rebuild
</ul>


<b>Changelog</b>

v0.45:
<ul>
<li>Support for ADW icon packs
<li>GET_CONTENT instead of ACTION_PICK intent for gallery image selection. Allowing more sources.
<li>Hiding icon pref when notification icon option is on
<li>Moved image picking to Commons from SettingsActivityX
<li>Do not use BitmapFactory to save gallery files
</ul>

v0.42
<ul>
<li>Fix for Android 4.2 caused by trying to parse Toasts
<li>Support for Android 4.2, albeit a few less features
<li>New icon. Thanks to Dima Skvarskyi 
<li>Option to use icon from notification
<li>Search in application selection activity
<li>Hundreds of new icons. Thanks to milosch lee 
<li>(New icon selection dialog)
<li>Automatic icon setting on application selection
<li>Improved application selection with better performance.
<li>Total extensions now up to 11.
<li>Ability to disable extensions. You can now have anywhere from 1 to 11 extensions in DashClock through DashNotifier
<li>Up button in all activities
<li>German translation
<li>Option to hide notification number
<li>Fixed duplicate notification count from WhatsApp and possibly other apps
</ul>



