package misc.migration.v1_1;


import io.ebean.annotation.DbDefault;
import io.ebean.annotation.DbMigration;
import io.ebean.annotation.History;
import io.ebean.annotation.HistoryExclude;
import io.ebean.annotation.NotNull;
import io.ebean.annotation.Platform;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(name = "migtest_e_history2")
@History
public class EHistory2 {

  @Id
  Integer id;

  @NotNull
  // see: https://mariadb.com/de/resources/blog/use-cases-for-mariadb-data-versioning/
  @DbMigration(preAlter = {
      "SET @@system_versioning_alter_history = 1",
      "update ${table} set ${column} = 'unknown' where ${column} is null"}, platforms = Platform.MARIADB)
  
  // other platforms
  @DbMigration(preAlter =  "update ${table} set ${column} = 'unknown' where ${column} is null")
  @DbDefault("unknown")
  String testString;

  @HistoryExclude
  String testString2;

  @NotNull
  @DbDefault("unknown")
  String testString3;

  @Size(max = 20)
  String newColumn;
}
