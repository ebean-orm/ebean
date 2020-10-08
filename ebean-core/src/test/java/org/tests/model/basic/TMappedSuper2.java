package org.tests.model.basic;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;

@MappedSuperclass
public class TMappedSuper2 implements Serializable {
  private static final long serialVersionUID = 325282672490816821L;

  String something;

  @Version
  int version;

  @Transient
  SomeObject someObject;

  @Transient
  Integer myint;

  public String getSomething() {
    return something;
  }

  public void setSomething(String something) {
    this.something = something;
  }

  public SomeObject getSomeObject() {
    return someObject;
  }

  public void setSomeObject(SomeObject someObject) {
    this.someObject = someObject;
  }

  public Integer getMyint() {
    return myint;
  }

  public void setMyint(Integer myint) {
    this.myint = myint;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

}

