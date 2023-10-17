package misc.migration.v1_2;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "migtest_fk_one")
public class DfkOne {

  @Id
  long id;
}
