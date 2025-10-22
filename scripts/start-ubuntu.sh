#!/usr/bin/env bash
set -euo pipefail

# Ubuntu-specific starter (same behavior as generic Linux script)
# Loads environment variables from .env and runs the packaged Spring Boot JAR.

: "${JAVA_OPTS:=}"
: "${ENV_FILE:=.env}"

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")"/.. && pwd)"
TARGET_DIR="$PROJECT_DIR/target"

# CLI parsing for profile selection
PROFILE_CLI=""
if [[ "${1:-}" == "-p" || "${1:-}" == "--profile" ]]; then
  PROFILE_CLI="${2:-}"
  if [[ -n "${PROFILE_CLI}" ]]; then shift 2; else echo "[start-ubuntu] Missing value for -p|--profile"; exit 2; fi
elif [[ "${1:-}" == --profile=* ]]; then
  PROFILE_CLI="${1#*=}"
  shift 1
fi

if [[ -f "$PROJECT_DIR/$ENV_FILE" ]]; then
  echo "[start-ubuntu] Loading environment vars from $ENV_FILE"
  set -a
  # shellcheck disable=SC1090
  source "$PROJECT_DIR/$ENV_FILE"
  set +a
else
  echo "[start-ubuntu] No $ENV_FILE found at project root; continuing with current environment"
fi

# Apply profile precedence: CLI > SPRING_PROFILES_ACTIVE (from env) > PROFILE alias
if [[ -n "$PROFILE_CLI" ]]; then
  export SPRING_PROFILES_ACTIVE="$PROFILE_CLI"
elif [[ -z "${SPRING_PROFILES_ACTIVE:-}" && -n "${PROFILE:-}" ]]; then
  export SPRING_PROFILES_ACTIVE="$PROFILE"
fi

JAR_FILE="${JAR_FILE:-}"
if [[ -z "$JAR_FILE" ]]; then
  DEFAULT_JAR="$TARGET_DIR/fico-processor-0.0.1-SNAPSHOT.jar"
  if [[ -f "$DEFAULT_JAR" ]]; then
    JAR_FILE="$DEFAULT_JAR"
  else
    CANDIDATE="$(ls -1t "$TARGET_DIR"/*.jar 2>/dev/null | head -n 1 || true)"
    if [[ -n "$CANDIDATE" ]]; then
      JAR_FILE="$CANDIDATE"
    fi
  fi
fi

if [[ -z "${JAR_FILE:-}" || ! -f "$JAR_FILE" ]]; then
  echo "[start-ubuntu] JAR not found. Building with Maven (skip tests)..."
  (cd "$PROJECT_DIR" && mvn -q -DskipTests package)
  JAR_FILE="${JAR_FILE:-$TARGET_DIR/fico-processor-0.0.1-SNAPSHOT.jar}"
fi

if [[ ! -f "$JAR_FILE" ]]; then
  echo "[start-ubuntu] ERROR: Could not locate the application JAR in $TARGET_DIR" >&2
  exit 1
fi

echo "[start-ubuntu] Starting application..."
echo "[start-ubuntu] Using JAR: $JAR_FILE"
if [[ -n "${SPRING_PROFILES_ACTIVE:-}" ]]; then
  echo "[start-ubuntu] SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE"
fi

EXTRA_ARGS=()
if [[ -n "${SPRING_PROFILES_ACTIVE:-}" ]]; then
  EXTRA_ARGS+=("--spring.profiles.active=${SPRING_PROFILES_ACTIVE}")
fi
exec java ${JAVA_OPTS} -jar "$JAR_FILE" "${EXTRA_ARGS[@]}"
