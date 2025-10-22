#!/usr/bin/env bash
set -euo pipefail

# --- Configuration (can be overridden via environment or .env) ---
: "${JAVA_OPTS:=}"
: "${ENV_FILE:=.env}"

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")"/.. && pwd)"
TARGET_DIR="$PROJECT_DIR/target"

# Simple CLI parsing for profile selection: -p|--profile <name> or --profile=name
PROFILE_CLI=""
if [[ "${1:-}" == "-p" || "${1:-}" == "--profile" ]]; then
  PROFILE_CLI="${2:-}"
  if [[ -n "${PROFILE_CLI}" ]]; then shift 2; else echo "[start-linux] Missing value for -p|--profile"; exit 2; fi
elif [[ "${1:-}" == --profile=* ]]; then
  PROFILE_CLI="${1#*=}"
  shift 1
fi

# Load environment variables from .env if present
if [[ -f "$PROJECT_DIR/$ENV_FILE" ]]; then
  echo "[start-linux] Loading environment vars from $ENV_FILE"
  set -a
  # shellcheck disable=SC1090
  source "$PROJECT_DIR/$ENV_FILE"
  set +a
else
  echo "[start-linux] No $ENV_FILE found at project root; continuing with current environment"
fi

# If CLI profile provided, override SPRING_PROFILES_ACTIVE
if [[ -n "$PROFILE_CLI" ]]; then
  export SPRING_PROFILES_ACTIVE="$PROFILE_CLI"
fi

# Allow PROFILE env var as alias if SPRING_PROFILES_ACTIVE not set
if [[ -z "${SPRING_PROFILES_ACTIVE:-}" && -n "${PROFILE:-}" ]]; then
  export SPRING_PROFILES_ACTIVE="$PROFILE"
fi

# Try to resolve the JAR file
JAR_FILE="${JAR_FILE:-}"
if [[ -z "${JAR_FILE}" ]]; then
  # default expected artifact name from pom.xml
  DEFAULT_JAR="$TARGET_DIR/fico-processor-0.0.1-SNAPSHOT.jar"
  if [[ -f "$DEFAULT_JAR" ]]; then
    JAR_FILE="$DEFAULT_JAR"
  else
    # try to pick the newest jar in target
    JAR_CANDIDATE="$(ls -1t "$TARGET_DIR"/*.jar 2>/dev/null | head -n 1 || true)"
    if [[ -n "$JAR_CANDIDATE" ]]; then
      JAR_FILE="$JAR_CANDIDATE"
    fi
  fi
fi

# Build if the jar was not found
if [[ -z "${JAR_FILE:-}" || ! -f "$JAR_FILE" ]]; then
  echo "[start-linux] JAR not found. Building with Maven (skip tests)..."
  (cd "$PROJECT_DIR" && mvn -q -DskipTests package)
  JAR_FILE="${JAR_FILE:-$TARGET_DIR/fico-processor-0.0.1-SNAPSHOT.jar}"
fi

if [[ ! -f "$JAR_FILE" ]]; then
  echo "[start-linux] ERROR: Could not locate the application JAR in $TARGET_DIR" >&2
  exit 1
fi

echo "[start-linux] Starting application..."
echo "[start-linux] Using JAR: $JAR_FILE"
if [[ -n "${SPRING_PROFILES_ACTIVE:-}" ]]; then
  echo "[start-linux] SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE"
fi

# Run the application
EXTRA_ARGS=()
if [[ -n "${SPRING_PROFILES_ACTIVE:-}" ]]; then
  EXTRA_ARGS+=("--spring.profiles.active=${SPRING_PROFILES_ACTIVE}")
fi
exec java ${JAVA_OPTS} -jar "$JAR_FILE" "${EXTRA_ARGS[@]}"
