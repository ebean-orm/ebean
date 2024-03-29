package org.tests.model.softdelete;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Version;

@Entity
public class ESoftDelOneB {

  @Id
  long id;

  String name;

  @OneToOne(mappedBy = "oneb")
  ESoftDelOneA onea;

  @Version
  long version;

  public ESoftDelOneB(String name) {
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

  public ESoftDelOneA getOnea() {
    return onea;
  }

  public void setOnea(ESoftDelOneA onea) {
    this.onea = onea;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
