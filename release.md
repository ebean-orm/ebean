## Release command

We @foconis use this command to release.

    mvn versions:set  -DgenerateBackupPoms=false -DnewVersion=12.14.2-FOC1-SNAPSHOT 
    mvn release:prepare release:perform -Darguments="-Dgpg.skip -DskipTests"
