package misc.migration.v1_1;


import io.ebean.annotation.DbDefault;
import io.ebean.annotation.History;
import io.ebean.annotation.NotNull;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "migtest_e_history5")
@History
public class EHistory5 {

  @Id
  Integer id;

  Integer testNumber;

  @NotNull
  @DbDefault("false")
  Boolean testBoolean;

}
