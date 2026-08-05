@ECHO OFF
TITLE ABookPlayer Launcher

SET "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"
SET "ANDROID_HOME=C:\Users\olexa\AppData\Local\Android\Sdk"
SET "PATH=%JAVA_HOME%\bin;%ANDROID_HOME%\platform-tools;C:\Users\olexa\AppData\AndroidCLI;%PATH%"

CD /D "%~dp0"

powershell -ExecutionPolicy Bypass -File "%~dp0run_app.ps1"

PAUSE
