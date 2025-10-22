@echo off
setlocal EnableExtensions EnableDelayedExpansion

rem --- Defaults (can be overridden) ---
set "ENV_FILE=.env"
set "JAVA_OPTS="
set "PROFILE_CLI="
set "JAR_FILE="
set "JAVA_OPTS_CLI="

rem --- Resolve project directory (repo root) ---
set "SCRIPT_DIR=%~dp0"
pushd "%SCRIPT_DIR%.." >nul
set "PROJECT_DIR=%CD%"
popd >nul
set "TARGET_DIR=%PROJECT_DIR%\target"

rem --- Parse CLI args ---
:parse_args
if "%~1"=="" goto args_done
if /I "%~1"=="-p" (
  set "PROFILE_CLI=%~2"
  shift & shift & goto parse_args
)
if /I "%~1"=="--profile" (
  set "PROFILE_CLI=%~2"
  shift & shift & goto parse_args
)
if /I "%~1"=="-profile" (
  set "PROFILE_CLI=%~2"
  shift & shift & goto parse_args
)
if /I "%~1"=="-EnvFile" (
  set "ENV_FILE=%~2"
  shift & shift & goto parse_args
)
if /I "%~1"=="-JarFile" (
  set "JAR_FILE=%~2"
  shift & shift & goto parse_args
)
if /I "%~1"=="-JavaOpts" (
  set "JAVA_OPTS_CLI=%~2"
  shift & shift & goto parse_args
)
rem Support --profile=dev style
for /f "tokens=1,2 delims==" %%G in ("%~1") do (
  if /I "%%~G"=="--profile" set "PROFILE_CLI=%%~H"
)
shift
goto parse_args
:args_done

rem --- Load environment variables from .env ---
if exist "%PROJECT_DIR%\%ENV_FILE%" (
  echo [start-windows.bat] Loading environment vars from %ENV_FILE%
  for /f "usebackq eol=# tokens=1,* delims==" %%A in ("%PROJECT_DIR%\%ENV_FILE%") do (
    set "k=%%~A"
    set "v=%%~B"
    if not "!k!"=="" (
      rem Trim spaces around key
      for /f "tokens=*" %%K in ("!k!") do set "k=%%~K"
      rem Set as is (no quote stripping); typical .env values work fine
      set "!k!=!v!"
    )
  )
) else (
  echo [start-windows.bat] No %ENV_FILE% found at project root; continuing with current environment
)

rem --- Apply profile precedence: CLI > SPRING_PROFILES_ACTIVE > PROFILE alias ---
if defined PROFILE_CLI (
  set "SPRING_PROFILES_ACTIVE=%PROFILE_CLI%"
) else (
  if not defined SPRING_PROFILES_ACTIVE if defined PROFILE set "SPRING_PROFILES_ACTIVE=%PROFILE%"
)

rem --- Prefer CLI-provided Java opts if set ---
if defined JAVA_OPTS_CLI set "JAVA_OPTS=%JAVA_OPTS_CLI%"

rem --- Resolve JAR file ---
if not defined JAR_FILE (
  set "DEFAULT_JAR=%TARGET_DIR%\fico-processor-0.0.1-SNAPSHOT.jar"
  if exist "%DEFAULT_JAR%" (
    set "JAR_FILE=%DEFAULT_JAR%"
  ) else (
    for /f "delims=" %%F in ('dir /b /o-d "%TARGET_DIR%\*.jar" 2^>nul') do (
      if not defined JAR_FILE set "JAR_FILE=%TARGET_DIR%\%%F"
    )
  )
)

rem --- Build if jar not found ---
if not defined JAR_FILE if exist "%PROJECT_DIR%\pom.xml" (
  echo [start-windows.bat] JAR not found. Building with Maven (skip tests)...
  pushd "%PROJECT_DIR%" >nul
  mvn -q -DskipTests package
  popd >nul
  if not defined JAR_FILE set "JAR_FILE=%TARGET_DIR%\fico-processor-0.0.1-SNAPSHOT.jar"
)

if not exist "%JAR_FILE%" (
  echo [start-windows.bat] ERROR: Could not locate the application JAR in %TARGET_DIR%
  exit /b 1
)

echo [start-windows.bat] Starting application...
echo [start-windows.bat] Using JAR: %JAR_FILE%
if defined SPRING_PROFILES_ACTIVE echo [start-windows.bat] SPRING_PROFILES_ACTIVE=%SPRING_PROFILES_ACTIVE%
if defined JAVA_OPTS echo [start-windows.bat] JAVA_OPTS=%JAVA_OPTS%

set "PROFILE_ARG="
if defined SPRING_PROFILES_ACTIVE set "PROFILE_ARG=--spring.profiles.active=%SPRING_PROFILES_ACTIVE%"

rem --- Run application ---
java %JAVA_OPTS% -jar "%JAR_FILE%" %PROFILE_ARG%

exit /b %ERRORLEVEL%
