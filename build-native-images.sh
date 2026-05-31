#!/usr/bin/env bash


# Enable testcontainers reuse
TC_PROPS="$HOME/.testcontainers.properties"
if ! grep -q "testcontainers.reuse.enable=true" "$TC_PROPS" 2>/dev/null; then
    echo "testcontainers.reuse.enable=true" >> "$TC_PROPS"
    echo "Set testcontainers.reuse.enable=true in $TC_PROPS"
fi

# build all modules - tests are run with native-image-agent to generate reachability-metadata

ALL_MODULES=(
    content-checker/content-checker-service
    email/email-service
    trend/trend-service
    main-app/report-service
    main-app/main-webapp
)

# copy native binaries to dist/
mkdir -p dist
for module in "${ALL_MODULES[@]}"; do
    name="${module##*/}"
    mvnd --batch-mode --no-transfer-progress --file "${module}/pom.xml"  test -Pnative
    mvnd --batch-mode --no-transfer-progress --file "${module}/pom.xml"  native:compile -Pnative -DskipTests

    src="$module/target/$name"
    cp "$src" "dist/$name"
done


docker build -f docker/Dockerfile.native --build-arg NATIVE_BINARY=dist/content-checker-service -t gtapp-content-checker-service-native:latest .
docker build -f docker/Dockerfile.native --build-arg NATIVE_BINARY=dist/email-service           -t gtapp-email-service-native:latest .
docker build -f docker/Dockerfile.native --build-arg NATIVE_BINARY=dist/trend-service           -t gtapp-trend-service-native:latest .
docker build -f docker/Dockerfile.native --build-arg NATIVE_BINARY=dist/report-service          -t gtapp-report-service-native:latest .
docker build -f docker/Dockerfile.native --build-arg NATIVE_BINARY=dist/main-webapp             -t gtapp-main-webapp-native:latest .
