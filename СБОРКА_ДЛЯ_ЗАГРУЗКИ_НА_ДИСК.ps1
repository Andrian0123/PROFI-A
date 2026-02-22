# Сборка APK для загрузки на Google Drive (раздача друзьям).
# Результат: app-debug.apk копируется в папку "для_загрузки" с именем PROFI-A-<version>-debug.apk

$ErrorActionPreference = "Stop"
$jbr = "C:\Program Files\Android\Android Studio\jbr"
if (-not (Test-Path "$jbr\bin\java.exe")) {
    Write-Host "JDK не найден: $jbr"
    exit 1
}
$env:JAVA_HOME = $jbr
Set-Location $PSScriptRoot

Write-Host "Сборка debug APK..."
.\gradlew.bat assembleDebug --no-daemon
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $apkPath)) {
    Write-Host "APK не найден: $apkPath"
    exit 1
}

# Версия из build.gradle.kts (упрощённо: можно заменить на парсинг файла)
$versionName = "1.0.0"
$versionFile = "app\build.gradle.kts"
if (Test-Path $versionFile) {
    $content = Get-Content $versionFile -Raw
    if ($content -match 'versionName\s*=\s*"([^"]+)"') {
        $versionName = $Matches[1]
    }
}

$outDir = "для_загрузки"
$outName = "PROFI-A-$versionName-debug.apk"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
Copy-Item -Path $apkPath -Destination (Join-Path $outDir $outName) -Force
# Копируем файл-инструкцию для загрузки на диск
$instructionFile = "ЗАГРУЗИТЬ_НА_ДИСК.txt"
if (Test-Path $instructionFile) {
    Copy-Item -Path $instructionFile -Destination (Join-Path $outDir $instructionFile) -Force
}
Write-Host "Готово: $outDir\$outName"
Write-Host "В папке $outDir также лежит $instructionFile — краткая инструкция для загрузки на Drive."
Write-Host "Подробнее: docs\ЗАГРУЗКА_НА_GOOGLE_DRIVE.md"
