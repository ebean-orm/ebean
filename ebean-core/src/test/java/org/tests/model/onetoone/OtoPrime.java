package org.tests.model.onetoone;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Version;

@Entity
public class OtoPrime {

  @Id
  Long pid;

  String name;

  /**
   * Automatically set Cascade PERSIST and mapped by.
   * OneToOne not optional so inner join to extra.
   */
  @OneToOne(optional = false, cascade = CascadeType.ALL)
  @PrimaryKeyJoinColumn
  OtoPrimeExtra extra;

  @Version
  Long version;

  public OtoPrime(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "id:"+ pid +" name:"+name+" extra:"+extra;
  }

  public Long getPid() {
    return pid;
  }

  public void setPid(Long pid) {
    this.pid = pid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public OtoPrimeExtra getExtra() {
    return extra;
  }

  public void setExtra(OtoPrimeExtra extra) {
    this.extra = extra;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}
