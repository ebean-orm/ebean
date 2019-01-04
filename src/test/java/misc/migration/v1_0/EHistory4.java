package misc.migration.v1_0;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.ebean.annotation.History;


@Entity
@Table(name = "migtest_e_history4")
@History
public class EHistory4 {

  @Id
  Integer id;

  Integer testNumber;

}
