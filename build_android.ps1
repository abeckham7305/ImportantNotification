# Simple build script for the Android app
Write-Host "Building Important Notification App..." -ForegroundColor Cyan

# Refresh PATH to make sure Java is available
$env:PATH = [System.Environment]::GetEnvironmentVariable("PATH","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("PATH","User")

# Check if we're in the right directory
if (-not (Test-Path "app\build.gradle")) {
    Write-Host "Error: Not in Android project directory" -ForegroundColor Red
    Write-Host "   Make sure you're in the ImportantNotification folder" -ForegroundColor Yellow
    exit 1
}

# Check for Java
Write-Host "`nChecking Java..." -ForegroundColor Yellow
try {
    $null = java -version 2>&1
    Write-Host "Java found" -ForegroundColor Green
} catch {
    Write-Host "Java not found. Please install Java first:" -ForegroundColor Red
    Write-Host "   winget install EclipseAdoptium.Temurin.17.JDK" -ForegroundColor White
    exit 1
}

# Create gradle wrapper if it doesn't exist
if (-not (Test-Path "gradlew.bat")) {
    Write-Host "`nCreating Gradle wrapper..." -ForegroundColor Yellow
    if (Get-Command gradle -ErrorAction SilentlyContinue) {
        gradle wrapper --gradle-version 8.1.1
    } else {
        Write-Host "Gradle not found. Creating wrapper manually..." -ForegroundColor Yellow
        # We'll let gradlew download itself on first run
    }
}

# Try to build
Write-Host "`nBuilding debug APK..." -ForegroundColor Yellow
if (Test-Path "gradlew.bat") {
    & .\gradlew.bat assembleDebug
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`nBuild successful!" -ForegroundColor Green
        Write-Host "APK location: app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor White
        
        # Check for connected devices
        $androidHome = $env:ANDROID_HOME
        if (-not $androidHome) {
            $androidHome = "$env:LOCALAPPDATA\Android\Sdk"
        }
        
        if (Test-Path "$androidHome\platform-tools\adb.exe") {
            Write-Host "`nConnected devices:" -ForegroundColor Yellow
            & "$androidHome\platform-tools\adb.exe" devices
            Write-Host "`nTo install: .\gradlew.bat installDebug" -ForegroundColor Cyan
        } else {
            Write-Host "`nTo install on device:" -ForegroundColor Cyan
            Write-Host "   1. Enable USB debugging on your phone" -ForegroundColor White
            Write-Host "   2. Connect via USB" -ForegroundColor White
            Write-Host "   3. Copy APK to phone and install manually" -ForegroundColor White
        }
    } else {
        Write-Host "`nBuild failed!" -ForegroundColor Red
    }
} else {
    Write-Host "Gradle wrapper not found" -ForegroundColor Red
}
