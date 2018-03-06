package org.tests.ddl;

import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class DfkNoneViaJoin {

  @Id
  long id;

  String name;

  @ManyToOne
  @JoinColumn(name = "one_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
  DfkOne one;

  public DfkNoneViaJoin(String name, DfkOne one) {
    this.name = name;
    this.one = one;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public DfkOne getOne() {
    return one;
  }

  public void setOne(DfkOne one) {
    this.one = one;
  }
}
