package org.tests.model.deleteorder;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

/**
 * Owned by a {@link DcoLink} through a cascading OneToOne, so deleting the link deletes the asset.
 */
@Entity
public class DcoAsset {

  @Id
  @GeneratedValue
  Long id;

  @Version
  Long version;

  String name;

  public DcoAsset(String name) {
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
