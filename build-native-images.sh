#!/usr/bin/env bash
set -euo pipefail

# ---------------------------------------------------------------------------
# Usage
# ---------------------------------------------------------------------------
usage() {
    echo "Usage: $0 <mode>"
    echo ""
    echo "  Modes:"
    echo "    metadata   Run tests with native-image-agent to generate reachability metadata"
    echo "               (./mvnw clean verify -Pnative-metadata)"
    echo "    build      Build native binary, skip tests"
    echo "               (./mvnw package -Pnative -DskipTests)"
    echo ""
    echo "  Example:"
    echo "    $0 metadata"
    echo "    $0 build"
    exit 1
}

# ---------------------------------------------------------------------------
# Validate required parameter
# ---------------------------------------------------------------------------
if [[ $# -ne 1 ]]; then
    echo "Error: exactly one argument required."
    usage
fi

MODE="$1"

case "$MODE" in
    metadata|build) ;;  # valid — fall through
    *)
        echo "Error: unknown mode '$MODE'."
        usage
        ;;
esac

# ---------------------------------------------------------------------------
# Modules
# ---------------------------------------------------------------------------
ALL_MODULES=(
    content-checker/content-checker-service
    email/email-service
    trend/trend-service
    main-app/report-service
    main-app/main-webapp
)

mkdir -p dist

for module in "${ALL_MODULES[@]}"; do
    name="${module##*/}"
    echo ">>> [$MODE] Building module: $name"

    case "$MODE" in
        metadata)
            ./mvnw --file "${module}/pom.xml" clean verify -Pnative-metadata
            ;;
        build)
            ./mvnw --file "${module}/pom.xml" package -Pnative -DskipTests
            src="$module/target/$name"
            ## optionally compress the binary using UPX
            #    upx --lzma "$src"
            cp "$src" "dist/$name"
            ;;
    esac
done

# ---------------------------------------------------------------------------
# Docker images (only meaningful after a 'build')
# ---------------------------------------------------------------------------
if [[ "$MODE" == "build" ]]; then
    echo ">>> Building Docker images..."
    docker build -f docker/Dockerfile.native --build-arg NATIVE_BINARY=dist/content-checker-service -t gtapp-content-checker-service-native:latest .
    docker build -f docker/Dockerfile.native --build-arg NATIVE_BINARY=dist/email-service           -t gtapp-email-service-native:latest .
    docker build -f docker/Dockerfile.native --build-arg NATIVE_BINARY=dist/trend-service           -t gtapp-trend-service-native:latest .
    docker build -f
fi

