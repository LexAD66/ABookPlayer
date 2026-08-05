<#
.SYNOPSIS
    Startet den visuellen Android-Emulator (GUI), baut die ABookPlayer-APK und führt die App aus.
    Stoppt gegebenenfalls vorher laufende Emulatoren für einen sauberen Neustart.
#>

# 1. Umgebungsvariablen setzen
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"
$env:ANDROID_HOME = "C:\Users\olexa\AppData\Local\Android\Sdk"
$env:PATH = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;C:\Users\olexa\AppData\AndroidCLI;$env:PATH"

$projectDir = $PSScriptRoot
Set-Location $projectDir

Write-Host "=== ABook Player: Test-Start Script ===" -ForegroundColor Cyan

# 2. Prüfen, ob ein Emulator läuft, und gegebenenfalls beenden
$runningEmulators = & adb devices | Select-String -Pattern "emulator-\d+"

if ($runningEmulators) {
    Write-Host "Bereits laufender Emulator gefunden. Beende alten Emulator..." -ForegroundColor Yellow
    foreach ($line in $runningEmulators) {
        $serial = $line.ToString().Split("`t")[0].Trim()
        if ($serial) {
            Write-Host "Stoppe $serial..." -ForegroundColor Yellow
            & adb -s $serial emu kill | Out-Null
        }
    }
    
    Write-Host "Warte bis der alte Emulator vollständig beendet ist..." -ForegroundColor Yellow
    do {
        Start-Sleep -Seconds 1
        $stillRunning = & adb devices | Select-String -Pattern "emulator-\d+"
    } while ($stillRunning)
    
    Write-Host "Alter Emulator erfolgreich gestoppt." -ForegroundColor Green
}

# 3. Frischen visuellen Emulator starten
Write-Host "Starte frischen visuellen Android-Emulator (GUI-Fenster)..." -ForegroundColor Yellow
Start-Process -FilePath "$env:ANDROID_HOME\emulator\emulator.exe" -ArgumentList "-avd", "medium_phone"

Write-Host "Warte auf Emulator-Verbindung..." -ForegroundColor Yellow
& adb wait-for-device

Write-Host "Warte bis Android OS vollständig hochgefahren ist..." -ForegroundColor Yellow
do {
    Start-Sleep -Seconds 2
    $booted = ""
    try {
        $booted = (& adb shell getprop sys.boot_completed).Trim()
    } catch {}
} while ($booted -ne "1")

Write-Host "Emulator ist bereit!" -ForegroundColor Green

# 4. APK kompilieren
Write-Host "Kompiliere Debug-APK..." -ForegroundColor Yellow
& "$projectDir\gradlew.bat" assembleDebug

# 5. APK auf Emulator installieren
$apkPath = "$projectDir\app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    Write-Host "Installiere APK auf dem Emulator..." -ForegroundColor Yellow
    & adb install -r $apkPath
    
    # 6. App auf dem Emulator öffnen
    Write-Host "Starte ABookPlayer..." -ForegroundColor Green
    & adb shell am force-stop de.f_soft_studio.abookplayer
    & adb shell am start -n de.f_soft_studio.abookplayer/.MainActivity
    Write-Host "Erfolgreich gestartet!" -ForegroundColor Green
} else {
    Write-Host "Fehler: APK wurde nicht gefunden unter $apkPath" -ForegroundColor Red
}
