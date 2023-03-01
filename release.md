## Merge back Robs master:


```bash
# First sync version to non-foc version. Version must be equal with rob's branch:
mvn versions:set  -DgenerateBackupPoms=false -DnewVersion=13.x.x-SNAPSHOT
git commit -am "Sync version to upstream"

# add remote (one time step)
git remote add upstream git@github.com:ebean-orm/ebean.git

git fetch upstream
git merge upstream/master
```
Now resolve all merge conflicts

```bash
# Set back to foc-version
mvn versions:set  -DgenerateBackupPoms=false -DnewVersion=13.x.x-FOCx-SNAPSHOT
```

Then check, if all -SNAPSHOT versions are foc-version


## Release command


We @foconis use this command to release.

```bash
mvn release:prepare release:perform -Darguments="-Dgpg.skip -DskipTests" -Pfoconis
```



    mvn versions:set  -DgenerateBackupPoms=false -DnewVersion=13.6.0-FOC2-SNAPSHOT

    mvn release:prepare release:perform -Darguments="-Dgpg.skip -DskipTests" -Pfoconis

    # RELEASE klappt nun, sollte es failen, ist wie folgt vorzugehen:
    # um bei einen Fehler zu release ist dann ins target/checkout Verzeichnis zu gehen und
    mvn clean source:jar install org.apache.maven.plugins:maven-deploy-plugin:deploy -DskipTests
    # auszuführen. Aber auch das failed bei Kotlin.

    # nach dem Release müssen die Versionen in ebean-kotlin/pom.xml, tests/test-java16/pom.xml und tests/test-kotlin/pom.xml manuell angepasst werden

    # auf gitHub Packages soll manuell deployed werden
    # commit mit dem Release Tag auschecken
    mvn deploy -Pgithub
    # wenn es Probleme mit Versionen gibt, dann manuell ebean-kotlin/pom.xml, tests/test-java16/pom.xml und tests/test-kotlin/pom.xml, usw. anpassen
    # wenn es bei kotlin-querybean-generator krachts, dann den Modul auskommentieren oder Modul auslassen und mit ... -rf :NÄCHSTE-MODUL weitermachen

## generate Java classes from .xsd:

    export JAVA_TOOL_OPTIONS="-Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8"
    /c/Program\ Files/Java/jdk1.8.0_201/bin/xjc.exe src/main/resources/ebean-dbmigration-1.0.xsd -d src/main/java -p io.ebeaninternal.dbmigration.migration
