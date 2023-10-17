package org.tests.model.pview;

import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

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
