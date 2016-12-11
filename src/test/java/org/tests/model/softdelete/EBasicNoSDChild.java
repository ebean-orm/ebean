package org.tests.model.softdelete;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

@Entity
public class EBasicNoSDChild {

  @Id
  Long id;

  @Version
  Long version;

  @ManyToOne(optional = false)
  EBasicSoftDelete owner;

  String childName;

  long amount;

  public EBasicNoSDChild(EBasicSoftDelete owner, String childName, long amount) {
    this.owner = owner;
    this.childName = childName;
    this.amount = amount;
  }

  public EBasicNoSDChild() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
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
