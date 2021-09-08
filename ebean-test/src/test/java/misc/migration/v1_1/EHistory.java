package misc.migration.v1_1;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.ebean.annotation.Platform;
import io.ebean.annotation.DbComment;
import io.ebean.annotation.DbMigration;
import io.ebean.annotation.History;

@Entity
@Table(name = "migtest_e_history")
@History
@DbComment("We have history now")
public class EHistory {

  @Id
  Integer id;

  @DbComment("Column altered to long now")
  @DbMigration(platforms = Platform.POSTGRES,
      preAlter = "alter table ${table} alter column ${column} TYPE bigint USING (${column}::integer)")
  Long testString;
}
