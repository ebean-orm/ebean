package misc.migration.v1_1;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(name = "migtest_e_index6")
// index at field level removed & altered
public class EIndex6 {

  @Id
  Integer id;

  @Size(max = 20)
  String string1;

  @Size(max = 20)
  String string2;

}
