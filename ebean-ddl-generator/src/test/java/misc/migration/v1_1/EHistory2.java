package misc.migration.v1_1;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import io.ebean.annotation.DbDefault;
import io.ebean.annotation.History;
import io.ebean.annotation.HistoryExclude;
import io.ebean.annotation.NotNull;

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
}
