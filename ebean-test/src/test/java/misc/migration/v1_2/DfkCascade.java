package misc.migration.v1_2;

import io.ebean.annotation.ConstraintMode;
import io.ebean.annotation.DbForeignKey;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "migtest_fk_cascade")
public class DfkCascade {

  @Id
  long id;

  @ManyToOne
  @DbForeignKey(onDelete = ConstraintMode.CASCADE)
  DfkCascadeOne one;

}
