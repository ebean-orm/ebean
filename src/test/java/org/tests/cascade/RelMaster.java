package org.tests.cascade;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

@Entity
public class RelMaster {

  @Id
  long id;

  String name;

  @Version
  int version;

  @ManyToOne(cascade = CascadeType.REMOVE)
  private RelDetail detail;

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

  public RelDetail getDetail() {
    return detail;
  }

  public void setDetail(RelDetail detail) {
    this.detail = detail;
  }


}
