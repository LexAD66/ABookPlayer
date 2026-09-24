@echo off
setlocal
cd /d "%~dp0"

echo ==========================================================
echo        ABook Player - GitHub Upload (Startskript)
echo        Ziel: https://github.com/LexAD66/ABookPlayer
echo ==========================================================
echo.

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0upload_github.ps1" %*

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [FEHLER] Upload-Prozess wurde mit Fehlercode %ERRORLEVEL% beendet.
    echo.
)

pause
