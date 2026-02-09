mdm_test_app — MDM / Kiosk integration guide

This repository demonstrates a minimal MDM (kiosk) integration for a
Flutter app using Android Device Policy APIs. Below are the exact files
and manifest changes you need to copy into another Flutter project to
reproduce the same functionality. Follow the steps carefully — some
operations (device owner provisioning, factory reset) are destructive.

What this adds
- Device admin support (wipe, force-lock, hide settings, lock-task)
- A Kotlin MDM manager (MdmManager.kt) with MethodChannel `kiosk_channel`
- Device admin XML (res/xml/device_admin.xml)
- DeviceAdminReceiver and BootReceiver Kotlin classes
- Flutter KioskService wrapper and a Kiosk / MDM Controls UI

Quick test
1. Add the files below to your project (paths shown).
2. Add the manifest entries and permission.
3. Build and run on a test device: `flutter run` (or `flutter build apk`).

Caution: Full kiosk features require device admin or device owner
privileges. Provisioning an app as device owner requires a factory reset
on most devices. Do not run provisioning on a personal device.

Files to copy (source → destination)
- android/app/src/main/kotlin/com/example/mdm_test_app/MdmManager.kt → place in the same package under android/app/src/main/kotlin/... in your app.
- android/app/src/main/kotlin/com/example/mdm_test_app/MyDeviceAdminReceiver.kt → copy to your package.
- android/app/src/main/kotlin/com/example/mdm_test_app/BootReceiver.kt → copy to your package.
- android/app/src/main/kotlin/com/example/mdm_test_app/MainActivity.kt → update your MainActivity to instantiate MdmManager in configureFlutterEngine (example included below).
- android/app/src/main/res/xml/device_admin.xml → copy to res/xml/device_admin.xml.
- android/app/src/main/AndroidManifest.xml → merge the changes outlined below into your app manifest.
- lib/kiosk_service.dart → copy into lib/ and import where required.
- lib/kiosk_mode.dart → optional: sample UI that calls the KioskService methods. Copy into lib/ and register the route in lib/main.dart.

MethodChannel name
- The native/Flutter channel name used is `kiosk_channel`. Ensure your Dart `MethodChannel('kiosk_channel')` matches the native side.

Manifest changes (merge into your AndroidManifest.xml)
- Add permission:
  - <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
- Register the device-admin receiver (inside <application>):
  <receiver
      android:name=".MyDeviceAdminReceiver"
      android:permission="android.permission.BIND_DEVICE_ADMIN"
      android:enabled="true"
      android:exported="true">
      <meta-data
          android:name="android.app.device_admin"
          android:resource="@xml/device_admin" />
      <intent-filter>
          <action android:name="android.app.action.DEVICE_ADMIN_ENABLED" />
      </intent-filter>
  </receiver>
- Register the boot receiver (inside <application>):
  <receiver
      android:name=".BootReceiver"
      android:exported="true">
      <intent-filter>
          <action android:name="android.intent.action.BOOT_COMPLETED" />
      </intent-filter>
  </receiver>

Device admin XML
- Copy res/xml/device_admin.xml which contains the policies your app will use, e.g. <force-lock/>, <wipe-data/>, <disable-keyguard-features/>.

Kotlin classes (what they do)
- MdmManager.kt: encapsulates all DevicePolicyManager calls and registers a MethodChannel named kiosk_channel. It implements methods:
  - enableKiosk() — sets lock-task packages and calls startLockTask()
  - disableKiosk() — calls stopLockTask() and clears lock-task packages
  - allowApps(List<String>) — adds allowed packages to lock-task list
  - blockSettings() / enableSettings() — hides/unhides com.android.settings
  - disableStatusBar() / enableStatusBar() — toggles status bar via DPM
  - factoryReset() — calls wipeData(0)
- MyDeviceAdminReceiver.kt: extends DeviceAdminReceiver (required for DPM callbacks). Keep it simple; metadata links to device_admin.xml.
- BootReceiver.kt: receives BOOT_COMPLETED and can launch the app on device boot. Requires the permission above.

MainActivity change
- Replace your existing MethodChannel / DPM code with a simple instantiation:
  class MainActivity : FlutterActivity() {
      override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
          super.configureFlutterEngine(flutterEngine)
          MdmManager(this, flutterEngine)
      }
  }

Dart changes
- lib/kiosk_service.dart: provides a KioskService wrapper calling the native methods via MethodChannel('kiosk_channel').
- lib/kiosk_mode.dart: sample UI that calls KioskService.enableKiosk(), KioskService.disableKiosk(), KioskService.blockSettings(), etc. Import the service with import 'kiosk_service.dart'; and ensure the route is registered in lib/main.dart.

Build & test
- Install dependencies and build:
  flutter pub get
  flutter build apk
- Run on device/emulator:
  flutter run -d <device-id>

Provisioning / enabling device admin
- To use DPM methods the user must enable your app as a device admin or the app must be device owner.
- To prompt the user to enable admin programmatically, launch the system intent ACTION_ADD_DEVICE_ADMIN from an Activity. Example Kotlin snippet:
  val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
  intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(this, MyDeviceAdminReceiver::class.java))
  intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "This app needs device admin for kiosk features.")
  startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
- To set device owner (testing only, requires factory reset):
  adb shell dpm set-device-owner "<your.package.name>/.MyDeviceAdminReceiver"
  This will only work on a freshly-wiped device or emulator.

Notes & caveats
- Many DPM calls require device owner — not just a device admin. Behavior will vary by OEM and Android version.
- wipeData() will factory reset the device — only expose that behind a confirmation and use extreme caution.
- setStatusBarDisabled() and setApplicationHidden() may be vendor-restricted on some devices.
- Android 12+ requires android:exported explicitly on components with intent-filters.

Files changed in this example (for reference)
- android/app/src/main/kotlin/com/example/mdm_test_app/MdmManager.kt
- android/app/src/main/kotlin/com/example/mdm_test_app/MyDeviceAdminReceiver.kt
- android/app/src/main/kotlin/com/example/mdm_test_app/BootReceiver.kt
- android/app/src/main/kotlin/com/example/mdm_test_app/MainActivity.kt
- android/app/src/main/res/xml/device_admin.xml
- android/app/src/main/AndroidManifest.xml
- lib/kiosk_service.dart
- lib/kiosk_mode.dart
- lib/main.dart

If you want, I can:
- Add a small UI button to launch the ACTION_ADD_DEVICE_ADMIN intent from Flutter.
- Add exact ADB provisioning commands and a safe checklist tailored to your test device.
- Help adapt these files to a different package name and scaffold a migration script.
