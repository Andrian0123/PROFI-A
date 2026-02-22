# Сборка debug-APK с JDK из Android Studio (если JAVA_HOME не задан)
$jbr = "C:\Program Files\Android\Android Studio\jbr"
if (-not (Test-Path "$jbr\bin\java.exe")) {
    Write-Host "JDK не найден: $jbr"
    exit 1
}
$env:JAVA_HOME = $jbr
Set-Location $PSScriptRoot
.\gradlew.bat assembleDebug --no-daemon
