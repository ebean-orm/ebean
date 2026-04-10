# Guides

Step-by-step guides written as instructions for AI agents and developers.

## Adding Ebean ORM with PostgreSQL to an existing Maven project

A two-part guide covering everything needed to wire Ebean + PostgreSQL into an
existing Maven project. Complete the steps in order.

| Step | Guide | Description |
|------|-------|-------------|
| 1 | [Maven POM setup](add-ebean-postgres-maven-pom.md) | Add Ebean dependencies, the enhancement plugin, and the querybean-generator annotation processor to `pom.xml` |
| 2 | [Database configuration](add-ebean-postgres-database-config.md) | Configure the Ebean `Database` bean using `DataSourceBuilder` and `DatabaseConfig` with Avaje Inject |
| 3 | [Test container setup](add-ebean-postgres-test-container.md) | Start a PostgreSQL (or PostGIS) Docker container for tests using `@TestScope @Factory` with Avaje Inject; covers image mirror, read-only datasource, and PostGIS variant |
