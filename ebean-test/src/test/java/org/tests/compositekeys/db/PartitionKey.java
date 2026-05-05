package org.tests.compositekeys.db;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PartitionKey implements Serializable {

  @Column(name = "org_id")
  private final Long orgId;
  @Column(name = "code")
  private final Long code;

  public PartitionKey(Long orgId, Long code) {
    this.orgId = orgId;
    this.code= code;
  }

  public Long orgId() {
    return orgId;
  }

  public Long code() {
    return code;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PartitionKey that = (PartitionKey) o;
    return Objects.equals(orgId, that.orgId) && Objects.equals(code, that.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orgId, code);
  }
}

