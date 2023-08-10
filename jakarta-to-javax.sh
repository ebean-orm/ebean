#!/usr/bin/env bash

## adjust pom dependencies
find . -type f -name 'pom.xml' -exec sed -i '' -e 's|<!-- JAKARTA-DEPENDENCY-START -->|<!-- JAKARTA-DEPENDENCY-START ___|g' {} +
find . -type f -name 'pom.xml' -exec sed -i '' -e 's|<!-- JAKARTA-DEPENDENCY-END -->|____ JAKARTA-DEPENDENCY-END -->|g' {} +
find . -type f -name 'pom.xml' -exec sed -i '' -e 's|<!-- JAVAX-DEPENDENCY-START ___|<!-- JAVAX-DEPENDENCY-START -->|g' {} +
find . -type f -name 'pom.xml' -exec sed -i '' -e 's|____ JAVAX-DEPENDENCY-END -->|<!-- JAVAX-DEPENDENCY-END -->|g' {} +

sed -i '' -e 's|artifactId>jakarta-persistence-api|artifactId>persistence-api|g' ebean-api/pom.xml ebean-bom/pom.xml

## adjust module-info
sed -i '' -e 's| jakarta\.persistence\.api| persistence\.api|g' ebean-api/src/main/java/module-info.java
sed -i '' -e 's| jakarta\.servlet;| javax\.servlet\.api;|g' ebean-api/src/main/java/module-info.java

find . -type f -name 'module-info.java' -exec sed -i '' -e 's|jakarta\.xml\.bind|java\.xml\.bind|g' {} +

## adjust code
find ebean-api/src/main/java/io/ebean/event -type f -name '*.java' -exec sed -i '' -e 's|jakarta\.servlet|javax\.servlet|g' {} +

find . -type f -name '*.java' -exec sed -i '' -e 's|jakarta\.persistence\.|javax\.persistence\.|g' {} +
find . -type f -name '*.kt' -exec sed -i '' -e 's|jakarta\.persistence\.|javax\.persistence\.|g' {} +
find . -type f -name '*.java' -exec sed -i '' -e 's|jakarta\.xml\.bind|javax\.xml\.bind|g' {} +
find . -type f -name '*.kt' -exec sed -i '' -e 's|jakarta\.xml\.bind|javax\.xml\.bind|g' {} +

find . -type f -name '*.java' -exec sed -i '' -e 's|jakarta\.transaction\.|javax\.transaction\.|g' {} +
