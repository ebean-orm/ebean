package org.tests.model.embedded;

import javax.persistence.*;

@Entity
public class EPerson3 {

  @Id
  long id;

  @Version
  long version;

  String name;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "street", column = @Column())
  })
  EAddress address;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public EAddress getAddress() {
    return address;
  }

  public void setAddress(EAddress address) {
    this.address = address;
  }

}
