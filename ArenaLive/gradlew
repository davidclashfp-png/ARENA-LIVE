#!/usr/bin/env bash
set -euo pipefail

GRADLE_VERSION="8.13"
BASE_DIR="${HOME}/.gradle/arenalive-bootstrap"
GRADLE_HOME_DIR="${BASE_DIR}/gradle-${GRADLE_VERSION}"
GRADLE_BIN="${GRADLE_HOME_DIR}/bin/gradle"

if [[ ! -x "${GRADLE_BIN}" ]]; then
  mkdir -p "${BASE_DIR}"
  ZIP_FILE="${BASE_DIR}/gradle-${GRADLE_VERSION}-bin.zip"
  echo "Baixando Gradle ${GRADLE_VERSION} (primeira execução)..."
  if command -v curl >/dev/null 2>&1; then
    curl -fL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o "${ZIP_FILE}"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "${ZIP_FILE}" "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
  else
    echo "É necessário instalar curl ou wget para baixar o Gradle." >&2
    exit 1
  fi
  unzip -q -o "${ZIP_FILE}" -d "${BASE_DIR}"
  rm -f "${ZIP_FILE}"
fi

exec "${GRADLE_BIN}" "$@"
