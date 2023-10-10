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


Note: Since Jakarta, Ebean is no longer released with the release-plugin by rob.


Solution1: Steps must be done manually:
```bash
# Set Version to next version with
mvn versions:set  -DgenerateBackupPoms=false -DnewVersion=13.x.x-FOCx
# commit everything
git add '*.xml'
git commit -m "bump version to release"
git tag 13.x.x-FOCx

#build and deploy it
mvn clean source:jar deploy -DskipTests -Pfoconis -T 8

mvn versions:set  -DgenerateBackupPoms=false -DnewVersion=13.x.x-FOCx+1-SNAPSHOT
git add '*.xml'
git commit -m "bump version to snapshot"
````


Solution 2: Ensure that you are on a snapshot version and use

```bash
mvn release:prepare release:perform -Darguments="-Dgpg.skip -DskipTests" -Pfoconis
```

### Build releases for github

First, checkout latest release commit with
```bash
git checkout HEAD~1
```

Build github release:

```bash
mvn clean source:jar deploy -DskipTests -Pgithub -T 8
```

## Fix POMs after release

After a release, you may have to fix poms with

```bash
mvn versions:update-parent -DallowSnapshots=true -DgenerateBackupPoms=false -Pjdk16plus -Pjdk15less
```

    # wenn es Probleme mit Versionen gibt, dann manuell ebean-kotlin/pom.xml, tests/test-java16/pom.xml und tests/test-kotlin/pom.xml, usw. anpassen
    # wenn es bei kotlin-querybean-generator krachts, dann den Modul auskommentieren oder Modul auslassen und mit ... -rf :NÄCHSTE-MODUL weitermachen

## generate Java classes from .xsd:

    export JAVA_TOOL_OPTIONS="-Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8"
    /c/Program\ Files/Java/jdk1.8.0_201/bin/xjc.exe src/main/resources/ebean-dbmigration-1.0.xsd -d src/main/java -p io.ebeaninternal.dbmigration.migration
