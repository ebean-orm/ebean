package misc.migration.v1_2;


import io.ebean.annotation.History;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "migtest_e_history5")
@History
public class EHistory5 {

  @Id
  Integer id;

  Integer testNumber;

}
