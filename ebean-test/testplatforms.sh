#!/bin/bash
# A small script, to run a certain test on all platforms
# invoke with ./testplatforms.sh -Dtest=DbMigrationTest
# Hint: in case of DbMigrationTest, you may disable ddl.run temporary 

# default H2 platform
set -e
mvn test "$@"

mvn surefire:test -Dprops.file=testconfig/ebean-mysql.properties "$@"

mvn surefire:test -Dprops.file=testconfig/ebean-mariadb.properties "$@"
mvn surefire:test -Dprops.file=testconfig/ebean-mariadb-10.3.properties "$@"

mvn surefire:test -Dprops.file=testconfig/ebean-sqlserver17.properties "$@"
mvn surefire:test -Dprops.file=testconfig/ebean-sqlserver19.properties "$@"

mvn surefire:test -Dprops.file=testconfig/ebean-postgres.properties "$@"

#mvn surefire:test -Dprops.file=testconfig/ebean-oracle.properties "$@"

#mvn surefire:test -Dprops.file=testconfig/ebean-sqlite.properties "$@"

mvn surefire:test -Dprops.file=testconfig/ebean-hana.properties "$@"

mvn surefire:test -Dprops.file=testconfig/ebean-db2.properties "$@"

## Test ignored
## mvn surefire:test -Dprops.file=testconfig/ebean-yugabyte.properties "$@"

## Scripts are not correct
## mvn surefire:test -Dprops.file=testconfig/ebean-cockroach.properties "$@"

## Transactions are not supported
## mvn surefire:test -Dprops.file=testconfig/ebean-clickhouse.properties "$@"

## I cannot start nuodb
## mvn surefire:test -Dprops.file=testconfig/ebean-nuodb.properties.properties "$@"


