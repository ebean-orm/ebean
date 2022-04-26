## Release command

We @foconis use this command to release.

    mvn versions:set  -DgenerateBackupPoms=false -DnewVersion=12.14.2-FOC1-SNAPSHOT 
    mvn release:prepare release:perform -Darguments="-Dgpg.skip -DskipTests"
    # das failed leider ab Ebean 13. Aktuell einzige Möglichkeit zu release ist dann ins target/checkout Verzeichnis zu gehen und
    mvn clean source:jar install org.apache.maven.plugins:maven-deploy-plugin:deploy -DskipTests
    # auszuführen. Aber auch das failed bei Kotlin.
    
generate Java classes from .xsd:

    export JAVA_TOOL_OPTIONS="-Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8"
    /c/Program\ Files/Java/jdk1.8.0_201/bin/xjc.exe src/main/resources/ebean-dbmigration-1.0.xsd -d src/main/java -p io.ebeaninternal.dbmigration.migration
