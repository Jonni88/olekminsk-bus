#!/bin/bash

# setup-gradle.sh - Downloads Gradle Wrapper

echo "📦 Setting up Gradle Wrapper..."

GRADLE_VERSION="8.2"
WRAPPER_URL="https://raw.githubusercontent.com/gradle/gradle/v${GRADLE_VERSION}/gradle/wrapper/gradle-wrapper.jar"

mkdir -p gradle/wrapper

# Download gradle-wrapper.jar
curl -L -o gradle/wrapper/gradle-wrapper.jar \
  "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"

echo "✅ Gradle wrapper setup complete!"
