
[![Build](https://github.com/ebean-orm/ebean/actions/workflows/build.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/build.yml)
[![Maven Central : ebean](https://maven-badges.herokuapp.com/maven-central/io.ebean/ebean/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.ebean/ebean)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/ebean-orm/ebean/blob/master/LICENSE)
[![Multi-JDK Build](https://github.com/ebean-orm/ebean/actions/workflows/multi-jdk-build.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/multi-jdk-build.yml)

##### Build with database platforms
[![H2Database](https://github.com/ebean-orm/ebean/actions/workflows/h2database.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/h2database.yml)
[![Postgres](https://github.com/ebean-orm/ebean/actions/workflows/postgres.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/postgres.yml)
[![MySql](https://github.com/ebean-orm/ebean/actions/workflows/mysql.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/mysql.yml)
[![MariaDB](https://github.com/ebean-orm/ebean/actions/workflows/mariadb.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/mariadb.yml)
[![Oracle](https://github.com/ebean-orm/ebean/actions/workflows/oracle.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/oracle.yml)
[![SqlServer](https://github.com/ebean-orm/ebean/actions/workflows/sqlserver.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/sqlserver.yml)
[![DB2 LUW](https://github.com/ebean-orm/ebean/actions/workflows/db2luw.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/db2luw.yml)
[![Yugabyte](https://github.com/ebean-orm/ebean/actions/workflows/yugabyte.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/yugabyte.yml)

##### Build with Java Early Access versions
[![ebean EA](https://github.com/ebean-orm/ebean/actions/workflows/jdk-ea.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/jdk-ea.yml)
[![datasource EA](https://github.com/ebean-orm/ebean-datasource/actions/workflows/jdk-ea.yml/badge.svg)](https://github.com/ebean-orm/ebean-datasource/actions/workflows/jdk-ea.yml)
[![migration EA](https://github.com/ebean-orm/ebean-migration/actions/workflows/jdk-ea.yml/badge.svg)](https://github.com/ebean-orm/ebean-migration/actions/workflows/jdk-ea.yml)
[![test-docker EA](https://github.com/ebean-orm/ebean-test-docker/actions/workflows/jdk-ea.yml/badge.svg)](https://github.com/ebean-orm/ebean-test-docker/actions/workflows/jdk-ea.yml)
[![ebean-agent EA](https://github.com/ebean-orm/ebean-agent/actions/workflows/jdk-ea.yml/badge.svg)](https://github.com/ebean-orm/ebean-agent/actions/workflows/jdk-ea.yml)

----------------------

# Ebean ORM for Java & Kotlin

**Multiple abstraction levels**: Ebean provides multiple levels of query abstraction [ORM Queries, mixed with SQL](https://ebean.io/docs/intro/queries/orm-query), [DTO Queries](https://ebean.io/docs/intro/queries/dto-query), [SqlQuery and JDBC](https://ebean.io/docs/intro/queries/sql-query).
Work at the highest level of abstraction and drop down levels as needed.

**Database migrations**: Built in [DB migration](https://ebean.io/docs/db-migrations/) generation and running. Support for "rebase" migrations as well as repeatable, init and 'normal' migrations.

**Awesome SQL**: Ebean produces SQL that you would hand craft yourself. Use great SQL, never generate SQL cartesian product, always honor relational limit/offset.

**Automated query tuning**: For ORM queries Ebean can profile the object graph being used and either [automatically tune the query](https://ebean.io/docs/query/background/autotune).

**Docker test containers**: [Docker test containers](https://ebean.io/docs/testing/) for all the supported databases. Get 100% test coverage on all the features of the database we use.

**Type safe queries**: We can build queries using type safe [query beans](https://ebean.io/docs/query/query-beans). IDE auto-complete when writing queries, compile time checking and it's FUN.

**Performance isn't optional**: Optimise queries to only fetch what we need (partial objects). Automatically avoid N+1 via a smart load context.

#### Benefits of ORM

* Automatically avoid N+1
* L2 caching to reduce database load
* Queries mixing database and L2 cache
* Automatically tune ORM queries
* Elasticsearch for search or L3 cache


----------------------
# Sponsors
<table>
  <tbody>
    <tr>
      <td align="center" valign="middle">
        <a href="https://www.foconis.de/" target="_blank">
          <img width="222px" src="https://group.foconis.com/download/ci/logo/png-72dpi/logo-quer/foconis-analytics-quer.png">
        </a>
      </td>
      <td align="center" valign="middle">
        <a href="https://www.premium-minds.com" target="_blank">
          <img width="222px" src="https://ebean.io/images/logo-med-principal.png">
        </a>
      </td>
      <td align="center" valign="middle">
        <a href="https://timerbee.de" target="_blank">
          <img width="222px" src="https://ebean.io/images/logo-timerbee.png">
        </a>
      </td>
    </tr>
  </tbody>
</table>

## Need help?
Post questions or issues to the [Ebean google group](https://groups.google.com/forum/#!forum/ebean)
or [github discussions](https://github.com/ebean-orm/ebean/discussions)

## Documentation
Goto [https://ebean.io/docs/](https://ebean.io/docs/)

## Maven central
[Maven central - g:io.ebean](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22io.ebean%22%20)

## Building Ebean from source

- JDK 11 or higher installed
- Maven installed
- `git clone git@github.com:ebean-orm/ebean.git`
- `mvn clean install`

Ebean 13 uses Java modules with module-info. This means that there are stricter compilation
rules in place now than when building with classpath pre version 13.

For Maven Surefire testing we use `<surefire.useModulePath>false</surefire.useModulePath>` such
that tests run using classpath and not module-path. We are doing this until all the tooling
(Maven, IDE) improves in the area of testing with module-info.

#### Eclipse IDE

Right now we can't use Eclipse IDE to build Ebean and run its tests due to its poor support
for java modules. See [ebean/issues/2653](https://github.com/ebean-orm/ebean/issues/2653)

The current recommendation is to use IntelliJ IDEA as the IDE to build and hack Ebean.


#### IntelliJ IDEA

We want to get IntelliJ to run tests using classpath similar to Maven Surefire. To do this set:
`JUnit -> modify options -> Do not use module-path option`

To set this option as the global default for IntelliJ use:

`Run - Edit Configurations -> Edit configuration templates -> JUnit -> modify options - Do not use module-path option`


end
