package misc.migration.v1_1;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.ebean.annotation.DbDefault;
import io.ebean.annotation.History;
import io.ebean.annotation.NotNull;


@Entity
@Table(name = "migtest_e_history6")
@History
public class EHistory6 {

  @Id
  Integer id;

  @NotNull
  @DbDefault("42")
  Integer testNumber1;


  Integer testNumber2;
}
