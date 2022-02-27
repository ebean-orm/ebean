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

SAP HANA
--------
To set up a SAP HANA container, you may need a docker account to download the image

1. Remove any existing HANA container with `docker rm hana`

2. Create an empty directory (e.g. `/tmp/hana`)

3. Create a `settings.json` file with content `{ "master_password": "VeryVerySecret#1234" }` in that directory

4. Run the container, e.g with this command:

```
docker run -p 39013:39013 -p 39017:39017 -p 39041-39045:39041-39045 -p 1128-1129:1128-1129 -p 59013-59014:59013-59014 \
    -v /tmp/hana:/hana/mounts \
    --ulimit nofile=1048576:1048576 --sysctl kernel.shmmax=1073741824 
    --sysctl net.ipv4.ip_local_port_range="40000 60999" --sysctl kernel.shmall=8388608 \
    --name "hana"  store/saplabs/hanaexpress:2.00.045.00.20200121.1  \
    --passwords-url file:///hana/mounts/settings.json --agree-to-sap-license
```

You can optionally add the `-d` parameter. Note, if you re-run that command, the best advice is to clear the data directory and recreate the settings.json file
