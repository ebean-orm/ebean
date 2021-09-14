package org.tests.delete;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class DcDetail {

  @Id
  long id;

  @ManyToOne
  DcMaster master;

  String description;

  long version;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public DcMaster getMaster() {
    return master;
  }

  public void setMaster(DcMaster master) {
    this.master = master;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
