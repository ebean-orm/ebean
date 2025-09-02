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
mvn -T 4 deploy -pl '!composites,!platforms' -Pcentral -DskipTests

## git commit, git tag, git push --tags
git commit -am 'Version 16.0.1'
git tag 16.0.1
git push --tags

## convert to javax
./jakarta-to-javax.sh

## set javax version
mvs

## deploy javax
mvn -T 4 clean package
mvn -T 4 deploy -pl '!composites,!platforms' -Pcentral -DskipTests

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
mvn -T 4 deploy -pl '!composites,!platforms' -Pcentral -DskipTests

```


