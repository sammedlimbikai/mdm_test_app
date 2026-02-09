package com.example.mdm_test_app

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MdmManager(private val activity: FlutterActivity, flutterEngine: FlutterEngine) {

    private val CHANNEL = "kiosk_channel"
    private val dpm: DevicePolicyManager = activity.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val admin: ComponentName = ComponentName(activity, MyDeviceAdminReceiver::class.java)

    private val channel: MethodChannel

    init {
        channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
        channel.setMethodCallHandler { call, result ->
            when (call.method) {
                "enableKiosk" -> {
                    try {
                        enableKiosk()
                        result.success(true)
                    } catch (e: Exception) {
                        Log.e("MdmManager", "enableKiosk failed", e)
                        result.error("ERROR", "enableKiosk failed: ${e.message}", null)
                    }
                }

                "disableKiosk" -> {
                    try {
                        disableKiosk()
                        result.success(true)
                    } catch (e: Exception) {
                        Log.e("MdmManager", "disableKiosk failed", e)
                        result.error("ERROR", "disableKiosk failed: ${e.message}", null)
                    }
                }

                "allowApps" -> {
                    val apps = call.argument<List<String>>("apps") ?: emptyList()
                    try {
                        allowApps(apps)
                        result.success(true)
                    } catch (e: Exception) {
                        Log.e("MdmManager", "allowApps failed", e)
                        result.error("ERROR", "allowApps failed: ${e.message}", null)
                    }
                }

                "blockSettings" -> {
                    try {
                        blockSettings()
                        result.success(true)
                    } catch (e: Exception) {
                        Log.e("MdmManager", "blockSettings failed", e)
                        result.error("ERROR", "blockSettings failed: ${e.message}", null)
                    }
                }

                "enableSettings" -> {
                    try {
                        enableSettings()
                        result.success(true)
                    } catch (e: Exception) {
                        Log.e("MdmManager", "enableSettings failed", e)
                        result.error("ERROR", "enableSettings failed: ${e.message}", null)
                    }
                }

                "disableStatusBar" -> {
                    try {
                        dpm.setStatusBarDisabled(admin, true)
                        result.success(true)
                    } catch (e: Exception) {
                        Log.e("MdmManager", "disableStatusBar failed", e)
                        result.error("ERROR", "disableStatusBar failed: ${e.message}", null)
                    }
                }

                "enableStatusBar" -> {
                    try {
                        dpm.setStatusBarDisabled(admin, false)
                        result.success(true)
                    } catch (e: Exception) {
                        Log.e("MdmManager", "enableStatusBar failed", e)
                        result.error("ERROR", "enableStatusBar failed: ${e.message}", null)
                    }
                }

                "factoryReset" -> {
                    try {
                        dpm.wipeData(0)
                        result.success(true)
                    } catch (e: Exception) {
                        Log.e("MdmManager", "factoryReset failed", e)
                        result.error("ERROR", "factoryReset failed: ${e.message}", null)
                    }
                }

                else -> result.notImplemented()
            }
        }
    }

    // 🔒 ENABLE FULL KIOSK
    private fun enableKiosk() {
        dpm.setLockTaskPackages(admin, arrayOf(activity.packageName))
        try {
            activity.startLockTask()
        } catch (e: Exception) {
            Log.w("MdmManager", "startLockTask exception: ${e.message}")
        }
    }

    // 🔓 DISABLE KIOSK (SAFE EXIT)
    private fun disableKiosk() {
        try {
            activity.stopLockTask()
        } catch (e: Exception) {
            Log.w("MdmManager", "stopLockTask exception: ${e.message}")
        }
        dpm.setLockTaskPackages(admin, emptyArray())
    }

    // ✅ ALLOW OTHER APPS
    private fun allowApps(packages: List<String>) {
        val allowed = packages.toMutableList()
        allowed.add(activity.packageName)
        dpm.setLockTaskPackages(admin, allowed.toTypedArray())
    }

    // 🚫 BLOCK SETTINGS
    private fun blockSettings() {
        dpm.setApplicationHidden(admin, "com.android.settings", true)
    }

    // ✅ ENABLE SETTINGS BACK
    private fun enableSettings() {
        dpm.setApplicationHidden(admin, "com.android.settings", false)
    }
}
