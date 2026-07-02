package org.tests.model.embedded;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

/**
 * Entity used to test @Embedded(nullable = false) —
 * the embedded address should never be null even when all address columns are null in the DB.
 */
@Entity
public class EPersonAllowNullRef {

  @Id
  Long id;

  @Version
  Long version;

  String name;

  @Embedded(nullable = false)
  EAddress address;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public EAddress getAddress() {
    return address;
  }

  public void setAddress(EAddress address) {
    this.address = address;
  }
}
