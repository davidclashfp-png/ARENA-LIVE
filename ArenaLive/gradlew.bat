@echo off
setlocal
set GRADLE_VERSION=8.13
set BASE_DIR=%USERPROFILE%\.gradle\arenalive-bootstrap
set GRADLE_HOME_DIR=%BASE_DIR%\gradle-%GRADLE_VERSION%
set GRADLE_BIN=%GRADLE_HOME_DIR%\bin\gradle.bat

if not exist "%GRADLE_BIN%" (
  if not exist "%BASE_DIR%" mkdir "%BASE_DIR%"
  echo Baixando Gradle %GRADLE_VERSION% (primeira execucao)...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%BASE_DIR%\gradle.zip'; Expand-Archive -Path '%BASE_DIR%\gradle.zip' -DestinationPath '%BASE_DIR%' -Force; Remove-Item '%BASE_DIR%\gradle.zip'"
  if errorlevel 1 exit /b 1
)

call "%GRADLE_BIN%" %*
exit /b %ERRORLEVEL%
