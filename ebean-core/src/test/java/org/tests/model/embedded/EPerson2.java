package org.tests.model.embedded;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class EPerson2 {

  @Id
  long id;

  @Version
  long version;

  String name;

  String notes;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "city", column = @Column(nullable = false)),
    @AttributeOverride(name = "status", column = @Column(nullable = false)),
    @AttributeOverride(name = "suburb", column = @Column(length = 100)),
    @AttributeOverride(name = "street", column = @Column(columnDefinition = "varchar(10)"))
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
