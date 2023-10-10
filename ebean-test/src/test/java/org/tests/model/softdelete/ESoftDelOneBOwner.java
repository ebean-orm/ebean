package org.tests.model.softdelete;

import io.ebean.annotation.SoftDelete;
import jakarta.persistence.*;

@SuppressWarnings("unused")
@Entity
public class ESoftDelOneBOwner {

  @Id
  long id;

  String name;

  @ManyToOne
  ESoftDelOneB oneb;

  @SoftDelete
  boolean deleted;

  @Version
  long version;

  public ESoftDelOneBOwner(String name) {
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

  public ESoftDelOneB oneb() {
    return oneb;
  }

  public void setOneb(ESoftDelOneB oneb) {
    this.oneb = oneb;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
