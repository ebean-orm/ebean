package misc.migration.v1_1;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.ebean.annotation.SoftDelete;

@Entity
@Table(name = "migtest_e_softdelete")
public class ESoftdelete {
  
  @Id
  Integer id;
  
  
  String testString;
  
  @SoftDelete
  boolean deleted;
}
