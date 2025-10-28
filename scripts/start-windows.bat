@echo off
setlocal EnableExtensions EnableDelayedExpansion

rem ======================================================================================
rem Fiserv Fico Processor - Windows starter (.bat)
rem Verbose run: prints each step, expected outcomes, command executed, and final result.
rem Parameters:
rem   -profile|-p <name>     Spring profile to activate
rem   -EnvFile <path>        .env file to load (default .env)
rem   -JarFile <path>        JAR to run (defaults to target\*.jar)
rem   -JavaOpts "opts"        Extra JVM options (overrides JAVA_OPTS from env)
rem Example:
rem   scripts\start-windows.bat -profile dev -EnvFile .env.dev -JavaOpts "-Xms256m -Xmx512m"
rem ======================================================================================

rem --- Defaults (can be overridden) ---
set "ENV_FILE=.env"
set "JAVA_OPTS="
set "PROFILE_CLI="
set "JAR_FILE="
set "JAVA_OPTS_CLI="
set "__STATUS=SUCCESS"

rem --- Resolve project directory (repo root) ---
set "SCRIPT_DIR=%~dp0"
pushd "%SCRIPT_DIR%.." >nul
set "PROJECT_DIR=%CD%"
popd >nul
set "TARGET_DIR=%PROJECT_DIR%\target"
echo [STEP 1/7] Resolve project paths
echo   EXPECT: PROJECT_DIR points to repo root, TARGET_DIR exists or will be created by build
echo   RESULT: PROJECT_DIR="%PROJECT_DIR%"
if exist "%TARGET_DIR%" (
  echo           TARGET_DIR exists: "%TARGET_DIR%"
) else (
  echo           TARGET_DIR not found yet: "%TARGET_DIR%" (will be created if build runs)
)

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

echo [STEP 2/7] Load environment file
echo   EXPECT: Read key=value pairs from "%ENV_FILE" and export to current process
if exist "%PROJECT_DIR%\%ENV_FILE%" (
  echo   CMD   : for /f (eol=#) "tokens=1,* delims==" %%A in ("%ENV_FILE%") do set %%A=%%B
  for /f "usebackq eol=# tokens=1,* delims==" %%A in ("%PROJECT_DIR%\%ENV_FILE%") do (
    set "k=%%~A"
    set "v=%%~B"
    if not "!k!"=="" (
      for /f "tokens=*" %%K in ("!k!") do set "k=%%~K"
      set "!k!=!v!"
    )
  )
  echo   RESULT: Loaded variables from %ENV_FILE%
) else (
  echo   RESULT: %ENV_FILE% not found at project root; using existing environment
)

rem --- Apply profile precedence: CLI > SPRING_PROFILES_ACTIVE > PROFILE alias ---
echo [STEP 3/7] Resolve Spring profile
if defined PROFILE_CLI (
  set "SPRING_PROFILES_ACTIVE=%PROFILE_CLI%"
  echo   CMD   : SPRING_PROFILES_ACTIVE set from CLI => "%SPRING_PROFILES_ACTIVE%"
) else (
  if not defined SPRING_PROFILES_ACTIVE if defined PROFILE set "SPRING_PROFILES_ACTIVE=%PROFILE%"
  if defined SPRING_PROFILES_ACTIVE (
    echo   RESULT: SPRING_PROFILES_ACTIVE from env/alias => "%SPRING_PROFILES_ACTIVE%"
  ) else (
    echo   RESULT: No active profile provided (Spring default will be used)
  )
)

rem --- Prefer CLI-provided Java opts if set ---
if defined JAVA_OPTS_CLI (
  set "JAVA_OPTS=%JAVA_OPTS_CLI%"
  echo [STEP 4/7] JVM options
  echo   CMD   : JAVA_OPTS taken from CLI => "%JAVA_OPTS%"
) else (
  echo [STEP 4/7] JVM options
  if defined JAVA_OPTS (
    echo   RESULT: JAVA_OPTS from env => "%JAVA_OPTS%"
  ) else (
    echo   RESULT: No JAVA_OPTS provided (defaults will be used)
  )
)

rem --- Resolve JAR file ---
echo [STEP 5/7] Locate application JAR
if not defined JAR_FILE (
  set "DEFAULT_JAR=%TARGET_DIR%\fico-processor-0.0.1-SNAPSHOT.jar"
  if exist "%DEFAULT_JAR%" (
    set "JAR_FILE=%DEFAULT_JAR%"
    echo   RESULT: Using default JAR => "%JAR_FILE%"
  ) else (
    for /f "delims=" %%F in ('dir /b /o-d "%TARGET_DIR%\*.jar" 2^>nul') do (
      if not defined JAR_FILE set "JAR_FILE=%TARGET_DIR%\%%F"
    )
    if defined JAR_FILE (
      echo   RESULT: Using latest JAR found in target => "%JAR_FILE%"
    ) else (
      echo   RESULT: No JAR found in target. A build will be attempted next.
    )
  )
) else (
  echo   RESULT: JAR provided via CLI => "%JAR_FILE%"
)

rem --- Build if jar not found ---
if not defined JAR_FILE if exist "%PROJECT_DIR%\pom.xml" (
  echo [STEP 6/7] Build project with Maven (skip tests)
  echo   CMD   : mvn -q -DskipTests package
  pushd "%PROJECT_DIR%" >nul
  mvn -q -DskipTests package
  set "__MVN_ERR=%ERRORLEVEL%"
  popd >nul
  if not defined JAR_FILE set "JAR_FILE=%TARGET_DIR%\fico-processor-0.0.1-SNAPSHOT.jar"
  if not "%__MVN_ERR%"=="0" (
    echo   FAIL  : Maven build failed with code %__MVN_ERR%
    set "__STATUS=FAILED"
    goto summary
  ) else (
    if exist "%JAR_FILE%" (
      echo   OK    : Build succeeded; jar is "%JAR_FILE%"
    ) else (
      echo   FAIL  : Build finished but jar not found in "%TARGET_DIR%"
      set "__STATUS=FAILED"
      goto summary
    )
  )
)

if not exist "%JAR_FILE%" (
  echo   FAIL  : Could not locate the application JAR in %TARGET_DIR%
  set "__STATUS=FAILED"
  goto summary
)

echo [STEP 7/7] Start application
set "PROFILE_ARG="
if defined SPRING_PROFILES_ACTIVE set "PROFILE_ARG=--spring.profiles.active=%SPRING_PROFILES_ACTIVE%"
set "__CMD=java %JAVA_OPTS% -jar \"%JAR_FILE%\" %PROFILE_ARG%"
echo   CMD   : %__CMD%
java %JAVA_OPTS% -jar "%JAR_FILE%" %PROFILE_ARG%
set "__APP_ERR=%ERRORLEVEL%"
if "%__APP_ERR%"=="0" (
  echo   OK    : Application exited with code 0
) else (
  echo   WARN  : Application exited with code %__APP_ERR%
  rem Non-zero may be expected if app runs until stopped; treat as informational
)

goto summary

:summary
echo.
echo [SUMMARY]
echo   Final status     : %__STATUS%
echo   JAR              : "%JAR_FILE%"
echo   Profile          : "%SPRING_PROFILES_ACTIVE%"
echo   ENV file         : "%ENV_FILE%"
echo   JAVA_OPTS        : "%JAVA_OPTS%"
echo   Working directory: "%PROJECT_DIR%"
exit /b %ERRORLEVEL%
