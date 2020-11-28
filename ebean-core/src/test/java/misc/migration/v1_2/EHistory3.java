package misc.migration.v1_2;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.ebean.annotation.History;


@Entity
@Table(name = "migtest_e_history3")
@History
public class EHistory3 {

  @Id
  Integer id;

  String testString;

}
