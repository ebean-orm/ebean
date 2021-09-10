package misc.migration.v1_0;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "migtest_fk_cascade_one")
public class DfkCascadeOne {

  @Id
  long id;

  @OneToMany(mappedBy = "one", cascade = CascadeType.ALL)
  List<DfkCascade> details;

}
