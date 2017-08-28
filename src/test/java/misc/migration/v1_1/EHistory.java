package misc.migration.v1_1;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.ebean.annotation.DbComment;
import io.ebean.annotation.History;

@Entity
@Table(name = "migtest_e_history")
@History
public class EHistory {
  
  @Id
  Integer id;
  
  @DbComment("Column has history now")
  String testString;
}
