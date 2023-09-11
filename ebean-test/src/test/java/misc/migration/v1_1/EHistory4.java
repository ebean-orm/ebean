package misc.migration.v1_1;


import io.ebean.annotation.History;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "migtest_e_history4")
@History
public class EHistory4 {

  @Id
  Integer id;

  Long testNumber;

}
