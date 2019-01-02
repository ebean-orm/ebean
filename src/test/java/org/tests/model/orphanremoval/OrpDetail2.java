package org.tests.model.orphanremoval;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class OrpDetail2 {

  @Id
  String id;

  String detail;

  String masterId;

  @Version
  long version;

  public OrpDetail2(String id, String detail, String masterId) {
    this.id = id;
    this.detail = detail;
    this.masterId = masterId;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getMasterId() {
    return masterId;
  }

  public void setMasterId(String masterId) {
    this.masterId = masterId;
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
