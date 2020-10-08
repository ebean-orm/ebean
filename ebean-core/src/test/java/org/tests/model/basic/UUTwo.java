package org.tests.model.basic;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import java.util.UUID;

@Entity
public class UUTwo {

  @Id
  UUID id;

  String name;

  String notes;

  @ManyToOne(cascade = CascadeType.PERSIST)
  UUOne master;

  @Version
  long version;

  public UUTwo() {
  }

  public UUTwo(String name, UUID id) {
    this.name = name;
    this.id = id;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
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

  public UUOne getMaster() {
    return master;
  }

  public void setMaster(UUOne master) {
    this.master = master;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
