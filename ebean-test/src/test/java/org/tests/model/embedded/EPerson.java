package org.tests.model.embedded;

import javax.persistence.*;

@Entity
public class EPerson {

  @Id
  Long id;

  @Version
  Long version;

  String name;

  String notes;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "city", column = @Column(name = "addr_city")),
    @AttributeOverride(name = "status", column = @Column(name = "addr_status")),
    @AttributeOverride(name = "jbean", column = @Column(name = "addr_jbean"))
  })
  EAddress address;

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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public EAddress getAddress() {
    return address;
  }

  public void setAddress(EAddress address) {
    this.address = address;
  }

}
