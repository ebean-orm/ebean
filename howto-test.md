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

By default, the `h2` platform is used. To test a different platform, you can change the 
`datasource.default` in the `ebean.properties` file.


Docker
------
Ebean 12 provides preconfigured docker containers now. To start a docker container,
you can use the starters in  `src/test/java/main` or specify one of the starter 
properties in the `testconfig` directory with `-Dprops.file=testconfig/ebean-XXX.properties`

Note: You will have to install docker on your system.
- For debian, you can use `apt install docker.io`
- For windows, install docker for windows https://docs.docker.com/desktop/windows/install/

Maven
-----

Use the `-Dprops.file` parameter to start the test cases for a certain platform.

