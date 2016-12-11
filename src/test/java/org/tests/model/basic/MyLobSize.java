package org.tests.model.basic;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class MyLobSize {

  @Id
  Integer id;

  String name;

  @Basic(fetch = FetchType.LAZY)
  int myCount;

  @Basic(fetch = FetchType.LAZY)
  @Lob
  //@Length(max=65535)
    //@Length(max=65536)
    String myLob;

  @OneToMany(mappedBy = "parent")
  List<MyLobSizeJoinMany> details;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getMyLob() {
    return myLob;
  }

  public void setMyLob(String myLob) {
    this.myLob = myLob;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getMyCount() {
    return myCount;
  }

  public void setMyCount(int myCount) {
    this.myCount = myCount;
  }

}
