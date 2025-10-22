Param(
  [string]$EnvFile = ".env",
  [string]$JarFile = "",
  [string]$Profile = "",
  [string]$JavaOpts = ""
)

$ErrorActionPreference = "Stop"

function Write-Info($msg) { Write-Host "[start-windows] $msg" }

function Load-DotEnv([string]$path) {
  if (-not (Test-Path -Path $path)) { return }
  Write-Info "Loading environment vars from $path"
  Get-Content -Path $path | ForEach-Object {
    $line = $_.Trim()
    if ($line -eq "" -or $line.StartsWith('#')) { return }
    $kv = $line -split '=', 2
    if ($kv.Length -eq 2) {
      $key = $kv[0].Trim()
      $val = $kv[1].Trim().Trim('"').Trim("'")
      [System.Environment]::SetEnvironmentVariable($key, $val)
    }
  }
}

# Resolve project directory (parent of this script)
$ProjectDir = Resolve-Path -Path (Join-Path $PSScriptRoot '..')
$TargetDir = Join-Path $ProjectDir 'target'

# Load .env (can be overridden by -EnvFile)
$env:ENV_FILE = if ($EnvFile) { $EnvFile } else { $env:ENV_FILE }
$envPath = Join-Path $ProjectDir $env:ENV_FILE
if (Test-Path -Path $envPath) { Load-DotEnv $envPath } else { Write-Info "No $($env:ENV_FILE) found at project root; continuing with current environment" }

# Apply profile precedence: CLI -Profile > SPRING_PROFILES_ACTIVE (already set) > PROFILE alias
if ($Profile -and $Profile.Trim() -ne '') {
  $env:SPRING_PROFILES_ACTIVE = $Profile.Trim()
} elseif (-not $env:SPRING_PROFILES_ACTIVE -and $env:PROFILE) {
  $env:SPRING_PROFILES_ACTIVE = $env:PROFILE
}

# Resolve jar file
if (-not $JarFile -or $JarFile.Trim() -eq '') {
  $defaultJar = Join-Path $TargetDir 'fico-processor-0.0.1-SNAPSHOT.jar'
  if (Test-Path -Path $defaultJar) {
    $JarFile = $defaultJar
  } else {
    $cand = Get-ChildItem -Path $TargetDir -Filter *.jar -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if ($cand) { $JarFile = $cand.FullName }
  }
}

# Build if not found
if (-not (Test-Path -Path $JarFile)) {
  Write-Info "JAR not found. Building with Maven (skip tests)..."
  Push-Location $ProjectDir
  mvn -q -DskipTests package
  Pop-Location
  if (-not $JarFile) { $JarFile = Join-Path $TargetDir 'fico-processor-0.0.1-SNAPSHOT.jar' }
}

if (-not (Test-Path -Path $JarFile)) {
  Write-Error "Could not locate the application JAR in $TargetDir"
}

Write-Info "Starting application..."
Write-Info "Using JAR: $JarFile"
if ($env:SPRING_PROFILES_ACTIVE) { Write-Info "SPRING_PROFILES_ACTIVE=$($env:SPRING_PROFILES_ACTIVE)" }

# Construct args
$extraArgs = @()
if ($env:SPRING_PROFILES_ACTIVE) { $extraArgs += "--spring.profiles.active=$($env:SPRING_PROFILES_ACTIVE)" }

# JAVA_OPTS can be provided via -JavaOpts or .env (JAVA_OPTS)
if ($JavaOpts) { $env:JAVA_OPTS = $JavaOpts }

# Execute
$cmd = "java $($env:JAVA_OPTS) -jar `"$JarFile`" $($extraArgs -join ' ')"
Write-Info "Command: $cmd"
# Use Start-Process to inherit console
& java $env:JAVA_OPTS -jar "$JarFile" @extraArgs