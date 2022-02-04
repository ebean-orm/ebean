## Release command

We @foconis use this command to release.

    mvn versions:set  -DgenerateBackupPoms=false -DnewVersion=12.14.2-FOC1-SNAPSHOT 
    mvn release:prepare release:perform -Darguments="-Dgpg.skip -DskipTests"
    
generate Java classes from .xsd:

    export JAVA_TOOL_OPTIONS="-Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8"
    /c/Program\ Files/Java/jdk1.8.0_201/bin/xjc.exe src/main/resources/ebean-dbmigration-1.0.xsd -d src/main/java -p io.ebeaninternal.dbmigration.migration
