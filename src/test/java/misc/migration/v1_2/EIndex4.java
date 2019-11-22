package misc.migration.v1_2;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import io.ebean.annotation.Index;

@Entity
@Table(name = "migtest_e_index4")
// index at field level
public class EIndex4 {

  @Id
  Integer id;

  @Index(unique = true)
  @Size(max = 10)
  String string1;

  @Index()
  @Size(max = 10)
  String string2;

}
