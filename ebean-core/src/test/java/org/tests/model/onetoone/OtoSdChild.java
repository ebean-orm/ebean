package org.tests.model.onetoone;

import io.ebean.Finder;
import io.ebean.annotation.SoftDelete;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Version;

@Entity
public class OtoSdChild {

  public static Finder<Long, OtoSdChild> find = new Finder<>(OtoSdChild.class);

  @Id
  long id;

  String child;

  @SoftDelete
  boolean deleted;

  @OneToOne
  OtoSdMaster master;

  @Version
  long version;

  public OtoSdChild(String child) {
    this.child = child;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getChild() {
    return child;
  }

  public void setChild(String child) {
    this.child = child;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public OtoSdMaster getMaster() {
    return master;
  }

  public void setMaster(OtoSdMaster master) {
    this.master = master;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
