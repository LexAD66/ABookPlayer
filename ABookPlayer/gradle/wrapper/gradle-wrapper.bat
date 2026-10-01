@ECHO OFF
IF DEFINED GRADLE_HOME (
    IF EXIST "%GRADLE_HOME%\bin\gradle.bat" (
        "%GRADLE_HOME%\bin\gradle.bat" %*
        EXIT /B %ERRORLEVEL%
    )
)
IF EXIST "%LOCALAPPDATA%\Gradle\gradle-8.7\bin\gradle.bat" (
    "%LOCALAPPDATA%\Gradle\gradle-8.7\bin\gradle.bat" %*
    EXIT /B %ERRORLEVEL%
)
WHERE gradle >nul 2>nul
IF %ERRORLEVEL% EQU 0 (
    gradle %*
    EXIT /B %ERRORLEVEL%
)
IF EXIST "%USERPROFILE%\AppData\Local\Gradle\gradle-8.7\bin\gradle.bat" (
    "%USERPROFILE%\AppData\Local\Gradle\gradle-8.7\bin\gradle.bat" %*
    EXIT /B %ERRORLEVEL%
)
ECHO Error: Gradle not found. Please install Gradle or set GRADLE_HOME.
EXIT /B 1
