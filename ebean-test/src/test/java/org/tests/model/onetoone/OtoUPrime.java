package org.tests.model.onetoone;

import io.ebean.annotation.DbForeignKey;

import javax.persistence.*;
import java.util.UUID;

@Entity
public class OtoUPrime {

  @Id
  UUID pid;

  String name;

  /**
   * Effectively Ebean automatically sets Cascade PERSIST and mapped by for PrimaryKeyJoinColumn.
   * This OneToOne is optional so left join to extra.
   */
  @OneToOne
  @PrimaryKeyJoinColumn
  @DbForeignKey(noConstraint = true)
  OtoUPrimeExtra extra;

  @Version
  Long version;

  public OtoUPrime(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "id:" + pid + " name:" + name + " extra:" + extra;
  }

  public UUID getPid() {
    return pid;
  }

  public void setPid(UUID pid) {
    this.pid = pid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public OtoUPrimeExtra getExtra() {
    return extra;
  }

  public void setExtra(OtoUPrimeExtra extra) {
    this.extra = extra;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}
