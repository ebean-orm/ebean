#!/usr/bin/env bash

## adjust pom dependencies
find . -type f -name 'pom.xml' -exec sed -i '' -e 's|<!-- JAVAX-DEPENDENCY-START -->|<!-- JAVAX-DEPENDENCY-START ___|g' {} +
find . -type f -name 'pom.xml' -exec sed -i '' -e 's|<!-- JAVAX-DEPENDENCY-END -->|____ JAVAX-DEPENDENCY-END -->|g' {} +
find . -type f -name 'pom.xml' -exec sed -i '' -e 's|<!-- JAKARTA-DEPENDENCY-START ___|<!-- JAKARTA-DEPENDENCY-START -->|g' {} +
find . -type f -name 'pom.xml' -exec sed -i '' -e 's|____ JAKARTA-DEPENDENCY-END -->|<!-- JAKARTA-DEPENDENCY-END -->|g' {} +

sed -i '' -e 's|artifactId>persistence-api|artifactId>jakarta-persistence-api|g' ebean-api/pom.xml ebean-bom/pom.xml

## adjust module-info
sed -i '' -e 's| persistence\.api| jakarta\.persistence\.api|g' ebean-api/src/main/java/module-info.java
sed -i '' -e 's| javax\.servlet\.api| jakarta\.servlet|g' ebean-api/src/main/java/module-info.java

find . -type f -name 'module-info.java' -exec sed -i '' -e 's|java\.xml\.bind|jakarta\.xml\.bind|g' {} +

## adjust code
find ebean-api/src/main/java/io/ebean/event -type f -name '*.java' -exec sed -i '' -e 's|javax\.servlet|jakarta\.servlet|g' {} +

find . -type f -name '*.java' -exec sed -i '' -e 's|javax\.persistence\.|jakarta\.persistence\.|g' {} +
find . -type f -name '*.kt' -exec sed -i '' -e 's|javax\.persistence\.|jakarta\.persistence\.|g' {} +
find . -type f -name '*.java' -exec sed -i '' -e 's|javax\.xml\.bind|jakarta\.xml\.bind|g' {} +
find . -type f -name '*.kt' -exec sed -i '' -e 's|javax\.xml\.bind|jakarta\.xml\.bind|g' {} +

