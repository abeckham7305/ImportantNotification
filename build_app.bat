@echo off
echo Building Important Notification App...

REM Set Android SDK path if not already set
if "%ANDROID_HOME%"=="" (
    if exist "%LOCALAPPDATA%\Android\Sdk" (
        set ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk
        echo Using Android SDK at: %ANDROID_HOME%
    ) else (
        echo ERROR: Android SDK not found. Please install Android SDK or run check_android_setup.bat
        pause
        exit /b 1
    )
)

REM Add Android SDK to PATH
set PATH=%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\tools;%PATH%

REM Create gradle wrapper if it doesn't exist
if not exist "gradlew.bat" (
    echo Creating Gradle wrapper...
    gradle wrapper --gradle-version 8.1.1
)

REM Clean and build the project
echo.
echo Cleaning project...
call gradlew.bat clean

echo.
echo Building debug APK...
call gradlew.bat assembleDebug

if %errorlevel% equ 0 (
    echo.
    echo ✓ Build successful!
    echo APK location: app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo To install on connected device, run:
    echo adb install app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo Or run: gradlew.bat installDebug
) else (
    echo.
    echo ✗ Build failed!
    echo Check the error messages above
)

echo.
pause
