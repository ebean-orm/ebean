package org.tests.model.onetoone;

import io.ebean.annotation.DbForeignKey;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class OtoUPrime {

  @Id
  UUID pid;

  String name;


  /**
   * Effectively Ebean automatically sets Cascade PERSIST and mapped by for PrimaryKeyJoinColumn.
   * This OneToOne is not optional so use inner join to extra (unless DbForeignkey(noConstraint = true) is set)
   * Note: Violating the contract (Storing OtoUPrime without extra) may cause problems:
   * - due the inner join, you might not get results from the query
   * - you might get a "Beah has been deleted" if lazy load occurs on 'extra'
   */
  @OneToOne(orphanRemoval = true, optional = false)
  @PrimaryKeyJoinColumn
  // enforcing left join - without 'noConstraint = true', an inner join is used
  @DbForeignKey(noConstraint = true)
  OtoUPrimeExtra extra;

  /**
   * This OneToOne is optional so left join to extra.
   * Setting FetchType.LAZY will NOT add the left join by default to the query.
   */
  @OneToOne(mappedBy = "prime", fetch = FetchType.LAZY, orphanRemoval = true, optional = true)
  OtoUPrimeOptionalExtra optionalExtra;

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

  public OtoUPrimeOptionalExtra getOptionalExtra() {
    return optionalExtra;
  }

  public void setOptionalExtra(OtoUPrimeOptionalExtra optionalExtra) {
    this.optionalExtra = optionalExtra;
  }
}
