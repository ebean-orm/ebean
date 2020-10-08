package org.tests.model.selfref;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "self_ref_customer")
public class SelfRefCustomer {

  @Id
  Long id;

  String name;

  @ManyToOne
  @JoinColumn(name = "referred_by_id")
  SelfRefCustomer referredBy;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public SelfRefCustomer getReferredBy() {
    return referredBy;
  }

  public void setReferredBy(SelfRefCustomer referredBy) {
    this.referredBy = referredBy;
  }

}
