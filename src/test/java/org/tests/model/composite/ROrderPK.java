package org.tests.model.composite;


import javax.persistence.Embeddable;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author rnentjes
 */
@Embeddable
public class ROrderPK implements Serializable {

  private static final long serialVersionUID = 7632735517186104883L;
  @Size(max=127)
  private String company;

  private Integer orderNumber;

  public ROrderPK() {
    this(null, null);
  }

  public ROrderPK(String company, Integer orderNumber) {
    this.company = company;
    this.orderNumber = orderNumber;
  }

  public Integer getOrderNumber() {
    return orderNumber;
  }

  public void setOrderNumber(Integer orderNumber) {
    this.orderNumber = orderNumber;
  }

  public String getCompany() {
    return company;
  }

  public void setCompany(String company) {
    this.company = company;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ROrderPK other = (ROrderPK) obj;
    if ((this.company == null) ? (other.company != null) : !this.company.equals(other.company)) {
      return false;
    }
    if (this.orderNumber == null || !this.orderNumber.equals(other.orderNumber)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 73 * hash + (this.company != null ? this.company.hashCode() : 0);
    hash = 73 * hash + (this.orderNumber != null ? this.orderNumber.hashCode() : 0);
    return hash;
  }


}

