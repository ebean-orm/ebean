package misc.migration.v1_2;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.ebean.annotation.History;


@Entity
@Table(name = "migtest_e_history5")
@History
public class EHistory5 {

  @Id
  Integer id;

  Integer testNumber;

}
