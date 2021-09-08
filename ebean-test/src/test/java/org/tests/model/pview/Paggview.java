package org.tests.model.pview;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "paggview")
public class Paggview {

  @OneToOne
  private Pview pview;

  @Basic(optional = false)
  private Integer amount;

  public Pview getPview() {
    return pview;
  }

  public void setPview(Pview pview) {
    this.pview = pview;
  }

  public Integer getAmount() {
    return amount;
  }

  public void setAmount(Integer amount) {
    this.amount = amount;
  }

}
