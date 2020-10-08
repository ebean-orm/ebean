package org.tests.model.softdelete;

import io.ebean.annotation.SoftDelete;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Version;

@Entity
public class ESoftDelOneA {

  @Id
  long id;

  String name;

  @OneToOne(cascade = CascadeType.ALL)
  ESoftDelOneB oneb;

  @SoftDelete
  boolean deleted;

  @Version
  long version;

  public ESoftDelOneA(String name) {
    this.name = name;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ESoftDelOneB getOneb() {
    return oneb;
  }

  public void setOneb(ESoftDelOneB oneb) {
    this.oneb = oneb;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
