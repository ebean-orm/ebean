## Release command

We @foconis use this command to release.

    mvn versions:set  -DgenerateBackupPoms=false -Prelease -DnewVersion=12.14.2-FOC1-SNAPSHOT 
    mvn release:prepare release:perform -Prelease -Darguments="-Dgpg.skip -DskipTests"
