package misc.migration.v1_2;

import io.ebean.annotation.DbForeignKey;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table(name = "migtest_fk_none")
public class DfkNone {

  @Id
  long id;

  @ManyToOne
  @DbForeignKey(noConstraint = true)
  DfkOne one;
}
