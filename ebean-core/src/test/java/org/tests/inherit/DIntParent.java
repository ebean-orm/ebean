package org.tests.inherit;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@Entity
@Table(name = "dint_parent")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.INTEGER)
public abstract class DIntParent {

  @Id
  private Long id;

  private Integer val;

  private String more;

  protected DIntParent(Integer val, String more) {
    this.val = val;
    this.more = more;
  }

  public abstract String getName();

  public Long getId() {
    return id;
  }

  public Integer getVal() {
    return val;
  }

  public String getMore() {
    return more;
  }

  public void setMore(String more) {
    this.more = more;
  }
}
