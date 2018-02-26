package misc.migration.v1_1;

import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "migtest_fk_none_via_join")
public class DfkNoneViaJoin {

  @Id
  long id;

  @ManyToOne
  @JoinColumn(name = "one_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
  DfkOne one;

}
