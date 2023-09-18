package org.tests.model.onetoone;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class OtoUPrimeWithConstraint {

  @Id
  UUID pid;

  String name;

  @OneToOne(orphanRemoval = true, optional = false)
  // @DbForeignKey(noConstraint = true) see OtoUPrime
  @PrimaryKeyJoinColumn
  OtoUPrimeExtraWithConstraint extra;

  @Version
  Long version;

  public OtoUPrimeWithConstraint(String name) {
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

  public OtoUPrimeExtraWithConstraint getExtra() {
    return extra;
  }

  public void setExtra(OtoUPrimeExtraWithConstraint extra) {
    this.extra = extra;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

}
