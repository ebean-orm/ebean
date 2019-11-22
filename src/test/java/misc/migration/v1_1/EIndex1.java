package misc.migration.v1_1;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import io.ebean.annotation.Index;

@Entity
@Table(name = "migtest_e_index1")
// shared index
@Index(name = "ix_migtest_e_index1", columnNames = { "string1", "string2" })
public class EIndex1 {

  @Id
  Integer id;

  @Size(max = 20)
  String string1;

  @Size(max = 20)
  String string2;

}
