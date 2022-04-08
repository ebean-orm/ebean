package org.multitenant.partition;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

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
