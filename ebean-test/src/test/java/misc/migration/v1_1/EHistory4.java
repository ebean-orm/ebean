package misc.migration.v1_1;


import io.ebean.annotation.History;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "migtest_e_history4")
@History
public class EHistory4 {

  @Id
  Integer id;

  Long testNumber;

}
