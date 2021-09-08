package org.tests.model.basic;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class MyLobSizeJoinMany {

  @Id
  Integer id;

  @Basic(fetch = FetchType.LAZY)
  String something;

  String other;

  @ManyToOne
  MyLobSize parent;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getSomething() {
    return something;
  }

  public void setSomething(String something) {
    this.something = something;
  }

  public String getOther() {
    return other;
  }

  public void setOther(String other) {
    this.other = other;
  }

  public MyLobSize getParent() {
    return parent;
  }

  public void setParent(MyLobSize parent) {
    this.parent = parent;
  }


}
