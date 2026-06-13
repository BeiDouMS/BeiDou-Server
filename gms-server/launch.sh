#!/bin/sh

# Use the bundled JRE or JAVA_HOME if set.
# This script assumes it is executed from the gms-server directory.
JAVA_DIR="${JAVA_HOME:-./jdk-21.0.11+10-jre}"
JAVA_EXEC="$JAVA_DIR/bin/java"

if [ ! -x "$JAVA_EXEC" ]; then
    echo "Error: Java executable not found or not executable at $JAVA_EXEC."
    exit 1
fi

# Run with basic logging and configuration.
# Redirecting stdout and stderr to server.log to allow monitoring.
"$JAVA_EXEC" -Dspring.config.location=application.yml -jar BeiDou.jar > server.log 2>&1 &

echo "BeiDou server started in background with PID: $!"
