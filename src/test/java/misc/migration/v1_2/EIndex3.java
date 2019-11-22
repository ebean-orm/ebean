package misc.migration.v1_2;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import io.ebean.annotation.Index;

@Entity
@Table(name = "migtest_e_index3")
// index on class level
@Index(name = "uq_migtest_e_index3", columnNames = { "string1" }, unique = true)
@Index(name = "ix_migtest_e_index3", columnNames = { "string2" })
public class EIndex3 {

  @Id
  Integer id;

  @Size(max = 10)
  String string1;

  @Size(max = 10)
  String string2;

}
