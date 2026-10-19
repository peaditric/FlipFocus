#!/bin/sh
# Lightweight bootstrap. Obtains the official Gradle 8.9 wrapper JAR when absent.
set -eu
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$JAR" ]; then
  echo '正在下载 Gradle 官方 wrapper（首次构建需要联网）...'
  curl -fL --retry 3 --connect-timeout 20 \
    'https://services.gradle.org/distributions/gradle-8.9-wrapper.jar' -o "$JAR" || {
    echo '下载失败，请检查网络或在 Android Studio 中配置 Gradle 8.9。'
    exit 1
  }
fi
exec java -classpath "$JAR" org.gradle.wrapper.GradleWrapperMain "$@"
