package misc.migration.v1_2;


import io.ebean.annotation.DbDefault;
import io.ebean.annotation.History;
import io.ebean.annotation.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "migtest_e_history6")
@History
public class EHistory6 {

  @Id
  Integer id;

  Integer testNumber1;

  @NotNull
  @DbDefault("7") // required for revert
  Integer testNumber2;
}
