# Deploy to Central

```shell
## confirm on master and building
git checkout master
mvn clean verify

## set the appropriate version
mvs

## run tests and package
mvn -T 4 clean package

## deploy
mvn -T 4 deploy -pl '!composites,!platforms' -Pcentral -DskipTests -DskipStagingRepositoryClose=true -DstagingProgressTimeoutMinutes=9

## git commit, git tag, git push --tags
git commit -am 'Version 14.9.0'
git tag 14.9.0
git push --tags

## convert to javax
./jakarta-to-javax.sh

## set javax version
mvs

## deploy javax
mvn -T 4 clean package
mvn -T 4 deploy -pl '!composites,!platforms' -Pcentral -DskipTests -DskipStagingRepositoryClose=true -DstagingProgressTimeoutMinutes=9

## checkout / cleanup
git checkout .

## goto ebean-15x branch
git checkout ebean-15x

## update ebean-15x branch from master and resolve conflicts
git merge master
## resolve conflicts
## git commit, git push

## set 15.x version
mvs

## build and deploy 15.x
mvn -T 4 clean package
mvn -T 4 deploy -pl '!composites,!platforms' -Pcentral -DskipTests -DskipStagingRepositoryClose=true -DstagingProgressTimeoutMinutes=9

```


