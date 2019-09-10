package misc.migration.v1_2;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import java.util.List;

@Entity
@Table(name = "migtest_fk_cascade_one")
public class DfkCascadeOne {

  @Id
  long id;

  @OneToMany(mappedBy = "one", cascade = CascadeType.ALL)
  List<DfkCascade> details;

}
