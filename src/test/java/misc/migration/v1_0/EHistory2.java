package misc.migration.v1_0;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.ebean.annotation.History;

@Entity
@Table(name = "migtest_e_history2")
@History
public class EHistory2 {
  
  @Id
  Integer id;
  
  
  String testString;
}
