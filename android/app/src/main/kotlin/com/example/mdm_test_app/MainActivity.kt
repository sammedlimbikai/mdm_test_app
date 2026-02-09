package com.example.mdm_test_app

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine

class MainActivity : FlutterActivity() {

	override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
		super.configureFlutterEngine(flutterEngine)
		// Delegate MDM / kiosk MethodChannel handling to MdmManager for cleaner separation
		MdmManager(this, flutterEngine)
	}
}
