package misc.migration.v1_1;


import io.ebean.annotation.DbMigration;
import io.ebean.annotation.History;
import io.ebean.annotation.Platform;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "migtest_e_history4")
@History
public class EHistory4 {

  @Id
  Integer id;

  @DbMigration(platforms = Platform.YUGABYTE, preAlter = {
      "alter table ${table} rename column ${column} to ${column}_tmp",
      "alter table ${table} add column ${column} bigint",
      "alter table ${table}_history rename column ${column} to ${column}_tmp",
      "alter table ${table}_history add column ${column} bigint",
      "update ${table} set ${column} = cast(${column}_tmp as bigint)",
      "update ${table}_history set ${column} = cast(${column}_tmp as bigint)",
      "commit transaction /* alter testNumber */"
  }, postAlter = {
      "alter table ${table} drop column ${column}_tmp",
      "alter table ${table}_history drop column ${column}_tmp" })
  Long testNumber;

}
