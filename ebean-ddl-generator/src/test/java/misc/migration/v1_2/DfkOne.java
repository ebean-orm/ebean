package misc.migration.v1_2;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "migtest_fk_one")
public class DfkOne {

  @Id
  long id;
}
