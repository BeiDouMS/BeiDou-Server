#!/bin/bash

# Build BeiDou JAR and copy to host
# Usage: ./build-jar.sh <output-path>

set -e

OUTPUT_PATH="${1:-.}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVER_DIR="/home/wudaown/git/BeiDou-Server"

echo "Building BeiDou JAR..."

# Run maven build in container and copy JAR
podman run --rm \
    -v "${SERVER_DIR}:/workspace" \
    -w /workspace \
    maven:3.9.6-amazoncorretto-21 \
    sh -c "mvn -f gms-server/pom.xml clean package -DskipTests -B"

JAR_PATH="${SERVER_DIR}/gms-server/target/BeiDou.jar"

if [ -f "$JAR_PATH" ]; then
    cp "$JAR_PATH" "$OUTPUT_PATH/"
    echo "JAR copied to: ${OUTPUT_PATH}/BeiDou.jar"
else
    echo "Error: JAR not found at $JAR_PATH"
    exit 1
fi
