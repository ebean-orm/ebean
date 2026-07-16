package org.tests.model.basic;

import io.ebean.annotation.Cache;
import io.ebean.annotation.EnumValue;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Cached bean with an Enum @Id (mapped via @EnumValue) for testing #3110.
 */
@Cache
@Entity
@Table(name = "e_cached_enum_id")
public class ECachedEnumId {

  public enum Status {
    @EnumValue("1")
    NEW,

    @EnumValue("2")
    APPROVED,

    @EnumValue("3")
    DELETED,
  }

  @Id
  Status status;

  String name;

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
