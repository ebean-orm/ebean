package misc.migration.v1_0;

import io.ebean.annotation.ConstraintMode;
import io.ebean.annotation.DbForeignKey;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "migtest_fk_cascade")
public class DfkCascade {

  @Id
  long id;

  @ManyToOne
  @DbForeignKey(onDelete = ConstraintMode.CASCADE)
  DfkCascadeOne one;

}
