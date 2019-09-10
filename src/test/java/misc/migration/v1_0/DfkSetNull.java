package misc.migration.v1_0;

import io.ebean.annotation.ConstraintMode;
import io.ebean.annotation.DbForeignKey;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "migtest_fk_set_null")
public class DfkSetNull {

  @Id
  long id;

  @ManyToOne
  @DbForeignKey(onDelete = ConstraintMode.SET_NULL)
  DfkOne one;

}
