package org.tests.model.orphanremoval;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import javax.validation.constraints.Size;

@Entity
public class OrpDetail {

  @Id
  @Size(max=100)
  String id;

  String detail;

  @ManyToOne
  OrpMaster master;

  @Version
  long version;

  public OrpDetail(String id, String detail) {
    this.id = id;
    this.detail = detail;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public OrpMaster getMaster() {
    return master;
  }

  public void setMaster(OrpMaster master) {
    this.master = master;
  }

  public String getDetail() {
    return detail;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
