package org.multitenant.partition;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class MtNone {

  @Id
  long id;

  final String none;

  @Version
  long version;

  public MtNone(String none) {
    this.none = none;
  }
}
