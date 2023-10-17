package misc.migration.v1_1;


import io.ebean.annotation.DbDefault;
import io.ebean.annotation.History;
import io.ebean.annotation.NotNull;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


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
