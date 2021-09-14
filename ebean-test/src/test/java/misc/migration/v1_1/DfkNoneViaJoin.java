package misc.migration.v1_1;

import javax.persistence.*;

@Entity
@Table(name = "migtest_fk_none_via_join")
public class DfkNoneViaJoin {

  @Id
  long id;

  @ManyToOne
  @JoinColumn(name = "one_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
  DfkOne one;

}
