package misc.migration.v1_1;

import io.ebean.annotation.ConstraintMode;
import io.ebean.annotation.DbForeignKey;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "migtest_fk_set_null")
public class DfkSetNull {

  @Id
  long id;

  @ManyToOne
  @DbForeignKey(onDelete = ConstraintMode.RESTRICT)
  DfkOne one;

}
