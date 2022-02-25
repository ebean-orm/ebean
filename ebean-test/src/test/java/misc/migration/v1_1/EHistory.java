package misc.migration.v1_1;


import io.ebean.annotation.DbComment;
import io.ebean.annotation.DbMigration;
import io.ebean.annotation.History;
import io.ebean.annotation.Platform;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "migtest_e_history")
@History
@DbComment("We have history now")
public class EHistory {

  @Id
  Integer id;

  @DbComment("Column altered to long now")
  @DbMigration(platforms = Platform.YUGABYTE, preAlter = {
      "alter table ${table} rename column ${column} to ${column}_tmp",
      "alter table ${table} add column ${column} bigint",
      "update ${table} set ${column} = cast(${column}_tmp as bigint)",
      "commit transaction /* alter testString */"
  }, postAlter = "alter table ${table} drop column ${column}_tmp")
  @DbMigration(platforms = Platform.POSTGRES,
      preAlter = "alter table ${table} alter column ${column} TYPE bigint USING (${column}::integer)")
  Long testString;
}
