This is a short description how to run tests
============================================

Run tests from maven
--------------------

Just type `mvn test` - it will run the whole test suite with the settings
from `src/test/resources/ebean.properties`. 

Run tests from eclipse
----------------------

Running tests requires, that the classes are enhanced. By default, the 
`BaseTestCase` tries to load the enhancer-agent in a very early stage. This 
also works for most test cases, but it does not work if you try to run a 
complete test-suite, as classes that are loaded before the enhancer-agent 
kicks in will not get enhanced. To solve this problem, you need either the
[Eclipse-Enhancer-Plugin](https://github.com/ebean-orm-tools/ebean-eclipse-enhancer) 
or add the agent to your JVM arguments (Preferences->Installed JREs) or to
your run configuration: 
`-javaagent:<USER_HOME>/.m2/repository/io/ebean/ebean-agent/<VERSION>/ebean-agent-<VERSION>.jar=debug=0`


Platform Tests
==============

By default, the `h2` platform is used. To test a different platform, add 
`-Ddatasource.default=xxx` to your maven command or JVM arguments.

'h2' platform
-------------

Reqires no setup

run: `mvn clean test`

Current status: PASS
Tests run: 1986, Failures: 0, Errors: 0, Skipped: 16

'pg' platform
-------------

Reqirements
- a locally installed PostgreSQL server with PostGis.
- Create a user "unit" with password "unit" and a database "unit"
- Give the user all permissions to that db

run: `mvn clean test -Ddatasource.default=pg`

Current status: PASS
Tests run: 2166, Failures: 0, Errors: 0, Skipped: 25



'mysql' platform
-------------

Reqirements
- a locally installed MySQL server.
- Create a user "unit" with password "unit" and a database "unit"
- Give the user all permissions to that db

run: `mvn clean test -Ddatasource.default=mysql`

Current status: PASS
Tests run: 2166, Failures: 0, Errors: 0, Skipped: 33



'sqlserver' platform
--------------------

Reqires an installed sqlserver - e.g. https://hub.docker.com/r/microsoft/mssql-server-linux/

- Create a user "ebean" with password "ebean" and a database "ebean_unittest"
- Adjust the connection string and/or username in ebean.properties
  
run: `mvn clean test -Ddatasource.default=mssql`

Current status: PASS
Tests run: 2166, Failures: 0, Errors: 0, Skipped: 39



'oracle' platform
-----------------

Reqires a locally installed Oracle server.
- Create a user "unit" with password "unit" with all permissions
- The test wirtes directly to the "xe" SID - so do not use a productive server!
- As the Oracle JDBC driver is only available on an oracle repository,
  you need a special maven setup as described [here](http://docs.oracle.com/middleware/1213/core/MAVEN/config_maven_repo.htm#MAVEN9010)
  
run: `mvn clean test -Ddatasource.default=ora -Poracle`

Current status: FAIL
Tests run: 1986, Failures: 17, Errors: 34, Skipped: 16



'db2' platform
--------------

Reqires a locally installed DB2 Express-C server.
- Create a user "unit" with password "unit" with all permissions
- Install the db2jcc4 driver (see pom.xml)

run: `mvn clean test -Ddatasource.default=db2 -Pdb2`

Current status: FAIL
Tests run: 2166, Failures: 29, Errors: 58, Skipped: 30


'sqlite' platform
-----------------

Reqires no setup

run: `mvn clean test -Ddatasource.default=sqlite`

Current status: FAIL
Tests run: 1986, Failures: 24, Errors: 86, Skipped: 16

After some time, the db locks up, every test takes 3 seconds and fails with 
"the database file is locked"



'hsqldb' platform
-----------------

Reqires no setup

run: `mvn clean test -Ddatasource.default=hsqldb`

Current status: FAIL
Tests run: 1980, Failures: 3, Errors: 657, Skipped: 16


@Rob FYI
The problem is https://sourceforge.net/p/hsqldb/bugs/1364/
In `InsertHandler.getPstmt` the `meta.getIdentityDbColumns()` contains
'id' in lowercase. Changing that to uppercase gives me this result:
`Tests run: 1986, Failures: 2, Errors: 18, Skipped: 16`
(as quick hack I added temporary a toUpperCase to BindableIdScalar.getIdentityColumn)


'sqlanywhere' platform
----------------------

TODO: not yet tested


