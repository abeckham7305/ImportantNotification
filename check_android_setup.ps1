# Android Setup Check for VS Code
Write-Host "Checking Android development setup..." -ForegroundColor Cyan

# Check Java
Write-Host "`nChecking Java..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1
    if ($javaVersion -match "version") {
        Write-Host "Java is installed:" -ForegroundColor Green
        Write-Host $javaVersion[0] -ForegroundColor White
    } else {
        throw "Java not found"
    }
} catch {
    Write-Host "Java is not installed or not in PATH" -ForegroundColor Red
    Write-Host "To install Java:" -ForegroundColor Yellow
    Write-Host "   1. Download from: https://adoptium.net/" -ForegroundColor White
    Write-Host "   2. Or install via Chocolatey: choco install temurin" -ForegroundColor White
    Write-Host "   3. Or install via winget: winget install EclipseAdoptium.Temurin.17.JDK" -ForegroundColor White
}

# Check Android SDK
Write-Host "`nChecking Android SDK..." -ForegroundColor Yellow
$androidHome = $env:ANDROID_HOME
if (-not $androidHome) {
    $androidHome = "$env:LOCALAPPDATA\Android\Sdk"
}

if (Test-Path "$androidHome\platform-tools\adb.exe") {
    Write-Host "Android SDK found at: $androidHome" -ForegroundColor Green
    $env:ANDROID_HOME = $androidHome
} else {
    Write-Host "Android SDK not found" -ForegroundColor Red
    Write-Host "To install Android SDK:" -ForegroundColor Yellow
    Write-Host "   1. Install Android Studio: https://developer.android.com/studio" -ForegroundColor White
    Write-Host "   2. Or use command line tools: https://developer.android.com/studio#command-tools" -ForegroundColor White
    Write-Host "   3. Expected location: $env:LOCALAPPDATA\Android\Sdk" -ForegroundColor White
}

# Check Gradle wrapper
Write-Host "`nChecking Gradle..." -ForegroundColor Yellow
if (Test-Path "gradlew.bat") {
    Write-Host "Gradle wrapper found" -ForegroundColor Green
} else {
    Write-Host "Gradle wrapper not found - will create during build" -ForegroundColor Yellow
}

# Check for connected devices
if (Test-Path "$androidHome\platform-tools\adb.exe") {
    Write-Host "`nChecking connected devices..." -ForegroundColor Yellow
    & "$androidHome\platform-tools\adb.exe" devices
}

Write-Host "`nNext Steps:" -ForegroundColor Cyan
Write-Host "   1. Install missing dependencies above" -ForegroundColor White
Write-Host "   2. Try building: Ctrl+Shift+P -> Tasks: Run Task -> Android: Build Debug APK" -ForegroundColor White
Write-Host "   3. Enable USB debugging on your phone for testing" -ForegroundColor White

Write-Host "`nSetup check complete!" -ForegroundColor Green
