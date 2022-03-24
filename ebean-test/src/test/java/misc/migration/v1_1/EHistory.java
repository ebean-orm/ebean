package misc.migration.v1_1;


import io.ebean.annotation.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "migtest_e_history")
@History
@DbComment("We have history now")
// TODO: Should we allow own table space for history?
@Tablespace("db2;MAIN;")
public class EHistory {

  @Id
  Integer id;

  @DbComment("Column altered to long now")
  @DbMigration(platforms = Platform.POSTGRES,
      preAlter = "alter table ${table} alter column ${column} TYPE bigint USING (${column}::integer)")
  Long testString;

  public Long getTestString() {
    return testString;
  }

  public void setTestString(Long testString) {
    this.testString = testString;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }
}
