param(
    [string]$Root = "$PSScriptRoot\..\"
)

$ErrorActionPreference = 'Stop'

# Idempotent generator for module stubs (build.gradle.kts + AndroidManifest.xml).
# Re-runs are safe: existing files are left untouched.

$libraryStub = @'
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.poweralarm.{NAMESPACE}"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions { jvmTarget = "21" }
    sourceSets["main"].kotlin.srcDir("src/main/kotlin")
    sourceSets["test"].kotlin.srcDir("src/test/kotlin")
}

dependencies {
    implementation(libs.kotlinx.coroutines)
}
'@

$manifestStub = @'
<?xml version="1.0" encoding="utf-8"?>
<manifest />
'@

$modules = @(
    @{ Path = 'core/domain';            Ns = 'core.domain' },
    @{ Path = 'core/data';              Ns = 'core.data' },
    @{ Path = 'core/scheduler';         Ns = 'core.scheduler' },
    @{ Path = 'core/audio';             Ns = 'core.audio' },
    @{ Path = 'core/permissions';       Ns = 'core.permissions' },
    @{ Path = 'core/logging';           Ns = 'core.logging' },
    @{ Path = 'feature/alarm-list';     Ns = 'feature.alarmlist' },
    @{ Path = 'feature/alarm-edit';     Ns = 'feature.alarmedit' },
    @{ Path = 'feature/ringer';         Ns = 'feature.ringer' },
    @{ Path = 'feature/settings';       Ns = 'feature.settings' },
    @{ Path = 'feature/themes';         Ns = 'feature.themes' },
    @{ Path = 'feature/statistics';     Ns = 'feature.statistics' },
    @{ Path = 'feature/profiles';       Ns = 'feature.profiles' },
    @{ Path = 'feature/wear';           Ns = 'feature.wear' },
    @{ Path = 'integrations/spotify';   Ns = 'integrations.spotify' },
    @{ Path = 'integrations/drive';     Ns = 'integrations.drive' },
    @{ Path = 'integrations/fitbit';    Ns = 'integrations.fitbit' },
    @{ Path = 'integrations/weather';   Ns = 'integrations.weather' },
    @{ Path = 'integrations/airquality';Ns = 'integrations.airquality' },
    @{ Path = 'integrations/traffic';   Ns = 'integrations.traffic' },
    @{ Path = 'integrations/tfl';       Ns = 'integrations.tfl' },
    @{ Path = 'integrations/calendar';  Ns = 'integrations.calendar' },
    @{ Path = 'integrations/holidays';  Ns = 'integrations.holidays' },
    @{ Path = 'integrations/tasker';    Ns = 'integrations.tasker' },
    @{ Path = 'integrations/cast';      Ns = 'integrations.cast' },
    @{ Path = 'integrations/emergency'; Ns = 'integrations.emergency' },
    @{ Path = 'integrations/smarthome'; Ns = 'integrations.smarthome' },
    @{ Path = 'integrations/nfc';       Ns = 'integrations.nfc' },
    @{ Path = 'integrations/rest-api';  Ns = 'integrations.restapi' }
)

foreach ($m in $modules) {
    $dir = Join-Path $Root $m.Path
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }

    $manifestDir = Join-Path $dir 'src/main'
    if (-not (Test-Path $manifestDir)) { New-Item -ItemType Directory -Path $manifestDir -Force | Out-Null }

    $kotlinDir = Join-Path $dir 'src/main/kotlin'
    if (-not (Test-Path $kotlinDir)) { New-Item -ItemType Directory -Path $kotlinDir -Force | Out-Null }

    $gradlePath = Join-Path $dir 'build.gradle.kts'
    if (-not (Test-Path $gradlePath)) {
        ($libraryStub -replace '\{NAMESPACE\}', $m.Ns) |
            Out-File -FilePath $gradlePath -Encoding utf8
    }

    $manifestPath = Join-Path $manifestDir 'AndroidManifest.xml'
    if (-not (Test-Path $manifestPath)) {
        $manifestStub | Out-File -FilePath $manifestPath -Encoding utf8
    }
}

Write-Output "Module stubs ensured for $($modules.Count) modules."
