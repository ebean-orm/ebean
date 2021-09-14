package org.tests.model.softdelete;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class EBasicSDChild extends BaseSoftDelete {

  @ManyToOne(optional = false)
  EBasicSoftDelete owner;

  String childName;

  long amount;

  public EBasicSDChild(EBasicSoftDelete owner, String childName, long amount) {
    this.owner = owner;
    this.childName = childName;
    this.amount = amount;
  }

  public EBasicSDChild() {
  }

  public EBasicSoftDelete getOwner() {
    return owner;
  }

  public void setOwner(EBasicSoftDelete owner) {
    this.owner = owner;
  }

  public String getChildName() {
    return childName;
  }

  public void setChildName(String childName) {
    this.childName = childName;
  }

  public long getAmount() {
    return amount;
  }

  public void setAmount(long amount) {
    this.amount = amount;
  }
}
