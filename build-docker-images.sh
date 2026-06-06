#!/bin/sh

# Enable testcontainers reuse
TC_PROPS="$HOME/.testcontainers.properties"
if ! grep -q "testcontainers.reuse.enable=true" "$TC_PROPS" 2>/dev/null; then
    echo "testcontainers.reuse.enable=true" >> "$TC_PROPS"
    echo "Set testcontainers.reuse.enable=true in $TC_PROPS"
fi

# create docker images out of build jar files using spring boot and packeto

mvnd spring-boot:build-image -pl content-checker/content-checker-service -DskipTests=true
mvnd spring-boot:build-image -pl email/email-service -DskipTests=true
mvnd spring-boot:build-image -pl trend/trend-service -DskipTests=true
mvnd spring-boot:build-image -pl main-app/main-webapp -DskipTests=true
mvnd spring-boot:build-image -pl main-app/report-service -DskipTests=true

# list
docker image ls | grep gtapp
