package misc.migration.v1_0;

import javax.persistence.*;

@Entity
@Table(name = "migtest_fk_none_via_join")
public class DfkNoneViaJoin {

  @Id
  long id;

  @ManyToOne
  @JoinColumn(name = "one_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
  DfkOne one;

}
