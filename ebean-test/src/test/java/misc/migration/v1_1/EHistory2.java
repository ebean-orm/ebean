package misc.migration.v1_1;


import io.ebean.annotation.DbDefault;
import io.ebean.annotation.History;
import io.ebean.annotation.HistoryExclude;
import io.ebean.annotation.NotNull;

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
  @DbDefault("unknown")
  String testString;

  @HistoryExclude
  String testString2;

  @NotNull
  @DbDefault("unknown")
  String testString3;

  @Size(max = 20)
  String newColumn;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getTestString() {
    return testString;
  }

  public void setTestString(String testString) {
    this.testString = testString;
  }

  public String getTestString2() {
    return testString2;
  }

  public void setTestString2(String testString2) {
    this.testString2 = testString2;
  }

  public String getTestString3() {
    return testString3;
  }

  public void setTestString3(String testString3) {
    this.testString3 = testString3;
  }

  public String getNewColumn() {
    return newColumn;
  }

  public void setNewColumn(String newColumn) {
    this.newColumn = newColumn;
  }

}
