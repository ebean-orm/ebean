package misc.migration.v1_1;


import io.ebean.annotation.SoftDelete;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "migtest_e_softdelete")
public class ESoftdelete {

  @Id
  Integer id;


  String testString;

  @SoftDelete
  boolean deleted;
}
