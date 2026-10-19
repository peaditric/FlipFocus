@ECHO OFF
SETLOCAL
SET "APP_HOME=%~dp0"
SET "WRAPPER_JAR=%APP_HOME%gradle\wrapper\gradle-wrapper.jar"
IF NOT EXIST "%WRAPPER_JAR%" (
  ECHO Downloading official Gradle wrapper JAR...
  powershell -NoProfile -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-8.9-wrapper.jar' -OutFile '%WRAPPER_JAR%'"
  IF ERRORLEVEL 1 EXIT /B 1
)
java -classpath "%WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*
ENDLOCAL
