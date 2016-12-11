package org.tests.model.basic;

import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.io.Serializable;
import java.sql.Timestamp;

@MappedSuperclass
public class BasicDomain implements Serializable {

  private static final long serialVersionUID = 5569496199004449769L;

  @Id
  Integer id;

  @WhenCreated
  Timestamp cretime;

  @WhenModified
  Timestamp updtime;

  @Version
  Long version;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Timestamp getUpdtime() {
    return updtime;
  }

  public void setUpdtime(Timestamp updtime) {
    this.updtime = updtime;
  }

  public Timestamp getCretime() {
    return cretime;
  }

  public void setCretime(Timestamp cretime) {
    this.cretime = cretime;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}
