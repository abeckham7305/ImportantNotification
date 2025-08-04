@echo off
echo Checking Android development setup...

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 8 or higher
    goto :end
) else (
    echo ✓ Java is installed
)

REM Check if Android SDK is installed
if not exist "%ANDROID_HOME%\platform-tools\adb.exe" (
    if not exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
        echo ERROR: Android SDK not found
        echo Please install Android SDK or set ANDROID_HOME environment variable
        goto :end
    ) else (
        echo ✓ Android SDK found at %LOCALAPPDATA%\Android\Sdk
        set ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk
    )
) else (
    echo ✓ Android SDK found at %ANDROID_HOME%
)

REM Check if Gradle is available
gradlew.bat --version >nul 2>&1
if %errorlevel% neq 0 (
    echo WARNING: Gradle wrapper not found, will create it
) else (
    echo ✓ Gradle wrapper is available
)

REM Check for connected devices
echo.
echo Checking for connected Android devices...
"%ANDROID_HOME%\platform-tools\adb.exe" devices
echo.

echo Android setup check complete!
echo You can now build the project with: build_app.bat
echo.

:end
pause
