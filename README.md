[![Build](https://github.com/ebean-orm/ebean/actions/workflows/build.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/build.yml)
[![Maven Central : ebean](https://maven-badges.herokuapp.com/maven-central/io.ebean/ebean/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.ebean/ebean)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/ebean-orm/ebean/blob/master/LICENSE)
[![Multi-JDK Build](https://github.com/ebean-orm/ebean/actions/workflows/multi-jdk-build.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/multi-jdk-build.yml)
[![JDK 18-ea](https://github.com/ebean-orm/ebean/actions/workflows/jdk-18-ea.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/jdk-18-ea.yml)

[![H2Database](https://github.com/ebean-orm/ebean/actions/workflows/h2database.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/h2database.yml)
[![Postgres](https://github.com/ebean-orm/ebean/actions/workflows/postgres.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/postgres.yml)
[![MySql](https://github.com/ebean-orm/ebean/actions/workflows/mysql.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/mysql.yml)
[![MariaDB](https://github.com/ebean-orm/ebean/actions/workflows/mariadb.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/mariadb.yml)
[![SqlServer](https://github.com/ebean-orm/ebean/actions/workflows/sqlserver.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/sqlserver.yml)
[![Yugabyte](https://github.com/ebean-orm/ebean/actions/workflows/yugabyte.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/yugabyte.yml)


#### Builds against EA (Early Access) versions of Java (19, Loom, panama etc)

[![ebean EA](https://github.com/ebean-orm/ebean/actions/workflows/jdk-ea.yml/badge.svg)](https://github.com/ebean-orm/ebean/actions/workflows/jdk-ea.yml)
[![datasource EA](https://github.com/ebean-orm/ebean-datasource/actions/workflows/jdk-ea.yml/badge.svg)](https://github.com/ebean-orm/ebean-datasource/actions/workflows/jdk-ea.yml)
[![migration EA](https://github.com/ebean-orm/ebean-migration/actions/workflows/jdk-ea.yml/badge.svg)](https://github.com/ebean-orm/ebean-migration/actions/workflows/jdk-ea.yml)
[![test-docker EA](https://github.com/ebean-orm/ebean-test-docker/actions/workflows/jdk-ea.yml/badge.svg)](https://github.com/ebean-orm/ebean-test-docker/actions/workflows/jdk-ea.yml)
[![ebean-agent EA](https://github.com/ebean-orm/ebean-agent/actions/workflows/jdk-ea.yml/badge.svg)](https://github.com/ebean-orm/ebean-agent/actions/workflows/jdk-ea.yml)


# Sponsors
<table>
  <tbody>
    <tr>
      <td align="center" valign="middle">
        <a href="https://www.foconis.de/" target="_blank">
          <img width="222px" src="https://www.foconis.de/templates/yootheme/cache/foconis_logo_322-709da1de.png">
        </a>
      </td>
      <td align="center" valign="middle">
        <a href="https://www.payintech.com/" target="_blank">
          <img width="222px" src="https://ebean.io/images/sponsor_PayinTech-logo-noir.png">
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
Post questions or issues to the Ebean google group - https://groups.google.com/forum/#!forum/ebean

## Documentation
Goto [https://ebean.io/docs/](https://ebean.io/docs/)

## Maven central
[Maven central - io.ebean](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22io.ebean%22%20)

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


