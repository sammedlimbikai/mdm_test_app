import 'package:flutter/services.dart';

class KioskService {
  static const _channel = MethodChannel('kiosk_channel');

  static Future<void> enableKiosk() async =>
      _channel.invokeMethod('enableKiosk');

  static Future<void> disableKiosk() async =>
      _channel.invokeMethod('disableKiosk');

  static Future<void> allowApps(List<String> apps) async =>
      _channel.invokeMethod('allowApps', {'apps': apps});

  static Future<void> blockSettings() async =>
      _channel.invokeMethod('blockSettings');

  static Future<void> enableSettings() async =>
      _channel.invokeMethod('enableSettings');

  static Future<void> disableStatusBar() async =>
      _channel.invokeMethod('disableStatusBar');

  static Future<void> enableStatusBar() async =>
      _channel.invokeMethod('enableStatusBar');

  static Future<void> factoryReset() async =>
      _channel.invokeMethod('factoryReset');
}
