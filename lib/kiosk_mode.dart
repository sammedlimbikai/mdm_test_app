import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'kiosk_service.dart';

class KioskModeScreen extends StatefulWidget {
  const KioskModeScreen({super.key});

  @override
  State<KioskModeScreen> createState() => _KioskModeScreenState();
}

class _KioskModeScreenState extends State<KioskModeScreen> {
  bool _blockBack = false;
  bool _immersive = false;
  bool _isLocked = false;

  void _enterImmersive() {
    SystemChrome.setEnabledSystemUIMode(SystemUiMode.immersiveSticky);
    setState(() => _immersive = true);
  }

  void _exitImmersive() {
    SystemChrome.setEnabledSystemUIMode(SystemUiMode.edgeToEdge);
    setState(() => _immersive = false);
  }

  @override
  Widget build(BuildContext context) {
    return PopScope(
      canPop: !_blockBack,
      child: Scaffold(
        appBar: AppBar(title: const Text('Kiosk / MDM Controls')),
        body: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              SwitchListTile(
                title: const Text('Block Back Button'),
                value: _blockBack,
                onChanged: (v) => setState(() => _blockBack = v),
              ),
              ListTile(
                title: const Text('Immersive (hide system bars)'),
                subtitle: Text(_immersive ? 'Active' : 'Inactive'),
                trailing: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    ElevatedButton(
                      onPressed: _enterImmersive,
                      child: const Text('Enter'),
                    ),
                    const SizedBox(width: 8),
                    ElevatedButton(
                      onPressed: _exitImmersive,
                      child: const Text('Exit'),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 12),
              Column(
                children: [
                  ElevatedButton(
                    onPressed: () async {
                      await KioskService.enableKiosk();
                      await KioskService.blockSettings();
                      await KioskService.disableStatusBar();
                      setState(() => _isLocked = true);
                    },
                    child: const Text("ENTER KIOSK MODE"),
                  ),

                  const SizedBox(height: 8),

                  ElevatedButton(
                    onPressed: () async {
                      await KioskService.enableStatusBar();
                      await KioskService.enableSettings();
                      await KioskService.disableKiosk();
                      setState(() => _isLocked = false);
                    },
                    child: const Text("EXIT KIOSK MODE (ADMIN)"),
                  ),

                  const SizedBox(height: 8),

                  ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.red,
                    ),
                    onPressed: () async {
                      // WARNING: factory reset; require caution
                      await KioskService.factoryReset();
                    },
                    child: const Text("FACTORY RESET (EMERGENCY)"),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              Text(
                'Status: ${_isLocked ? 'Locked (lockTask active)' : 'Unlocked'}',
              ),
              const SizedBox(height: 12),
              Text(
                'Notes: lockTask may require device owner / provisioning on Android.',
              ),
            ],
          ),
        ),
      ),
    );
  }
}
