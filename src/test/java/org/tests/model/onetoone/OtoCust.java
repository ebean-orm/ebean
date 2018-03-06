package org.tests.model.onetoone;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Version;

@Entity
public class OtoCust {

  @Id
  long cid;

  String name;

  /**
   * Orphan removal so must delete 'old' address when it has been replace or set to null.
   */
  @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
  OtoCustAddress address;

  @Version
  long version;

  public OtoCust(String name) {
    this.name = name;
  }

  public long getCid() {
    return cid;
  }

  public void setCid(long cid) {
    this.cid = cid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public OtoCustAddress getAddress() {
    return address;
  }

  public void setAddress(OtoCustAddress address) {
    this.address = address;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
