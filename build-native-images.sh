#!/usr/bin/env bash


# build all modules - tests are run with native-image-agent to generate reachability-metadata

ALL_MODULES=(
    content-checker/content-checker-service
    email/email-service
    trend/trend-service
    main-app/report-service
    main-app/main-webapp
)

./mvnw clean

# copy native binaries to dist/
mkdir -p dist
for module in "${ALL_MODULES[@]}"; do
    name="${module##*/}"
    ./mvnw --batch-mode --no-transfer-progress --file "${module}/pom.xml" package -Pnative -DskipTests
    src="$module/target/$name"

## optionally compress the binary using UPX
#    upx --lzma "$src"

    cp "$src" "dist/$name"
done


docker build -f docker/Dockerfile.native --build-arg NATIVE_BINARY=dist/content-checker-service -t gtapp-content-checker-service-native:latest .
docker build -f docker/Dockerfile.native --build-arg NATIVE_BINARY=dist/email-service           -t gtapp-email-service-native:latest .
docker build -f docker/Dockerfile.native --build-arg NATIVE_BINARY=dist/trend-service           -t gtapp-trend-service-native:latest .
docker build -f docker/Dockerfile.native --build-arg NATIVE_BINARY=dist/report-service          -t gtapp-report-service-native:latest .
docker build -f docker/Dockerfile.native --build-arg NATIVE_BINARY=dist/main-webapp             -t gtapp-main-webapp-native:latest .
