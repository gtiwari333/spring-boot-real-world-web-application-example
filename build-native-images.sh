#!/usr/bin/env bash

# ---------------------------------------------------------------------------
# Modules
# ---------------------------------------------------------------------------
ALL_MODULES=(
#    content-checker/content-checker-service
#    email/email-service
#    trend/trend-service
#    main-app/report-service
    main-app/main-webapp
)

mkdir -p dist

for module in "${ALL_MODULES[@]}"; do
    name="${module##*/}"
    echo ">>> [$MODE] Building module: $name"
            ./mvnw --file "${module}/pom.xml" native:compile -Pnative

            src="$module/target/$name"
            ## optionally compress the binary using UPX
            #    upx --lzma "$src"
            cp "$src" "dist/$name"
done

# ---------------------------------------------------------------------------
# Docker images
# ---------------------------------------------------------------------------
docker build -f docker/Dockerfile.native --build-arg NATIVE_BINARY=dist/content-checker-service -t gtapp-content-checker-service-native:latest .
docker build -f docker/Dockerfile.native --build-arg NATIVE_BINARY=dist/email-service           -t gtapp-email-service-native:latest .
docker build -f docker/Dockerfile.native --build-arg NATIVE_BINARY=dist/trend-service           -t gtapp-trend-service-native:latest .
docker build -f docker/Dockerfile.native --build-arg NATIVE_BINARY=dist/report-service          -t gtapp-report-service-native:latest .
docker build -f docker/Dockerfile.native --build-arg NATIVE_BINARY=dist/main-webapp             -t gtapp-main-webapp-native:latest .

