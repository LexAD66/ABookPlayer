# Auto-Install Directive (Option B)

Nach jeder erfolgreichen Änderung und Verifikation im gesamten Projekt (in allen Chat-Sitzungen) gilt strikt die **Option B**:

1. **Automatische Installation (Direkt-Deployment):**
   - Nach erfolgreichem Kompilieren und Testen wird automatisch versucht, das Update mit `.\gradlew.bat installDebug` direkt auf ein angeschlossenes Smartphone/Emulator zu übertragen.

2. **Verhalten bei fehlender USB-Verbindung:**
   - Falls kein Gerät angeschlossen ist (`No connected devices`), wird die gebaute Debug-APK ([`app-debug.apk`](file:///c:/Users/olexa/Documents/MayDev/ABookPlayer/ABookPlayer/app/build/outputs/apk/debug/app-debug.apk)) bereitgestellt und der Nutzer informiert, dass die APK bereitsteht, sobald das Gerät per USB angeschlossen wird.
