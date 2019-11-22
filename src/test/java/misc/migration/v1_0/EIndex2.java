package misc.migration.v1_0;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import io.ebean.annotation.Index;

@Entity
@Table(name = "migtest_e_index2")
// shared unique index
@Index(name = "uq_migtest_e_index2", columnNames = { "string1", "string2" }, unique = true)
public class EIndex2 {

  @Id
  Integer id;

  @Size(max = 10)
  String string1;

  @Size(max = 10)
  String string2;

}
