package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.Version;
import java.time.LocalDate;

@Entity
@Inheritance
public abstract class VehicleLease {

  @Id
  long id;

  String name;

  LocalDate activeStart;

  LocalDate activeEnd;

  @Version
  long version;

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

  public LocalDate getActiveStart() {
    return activeStart;
  }

  public void setActiveStart(LocalDate activeStart) {
    this.activeStart = activeStart;
  }

  public LocalDate getActiveEnd() {
    return activeEnd;
  }

  public void setActiveEnd(LocalDate activeEnd) {
    this.activeEnd = activeEnd;
  }
}
