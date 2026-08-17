package org.tests.model.deleteorder;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

/**
 * Join entity between {@link DcoParent} and {@link DcoAsset}. It owns the foreign key to the asset,
 * so the link row has to be deleted before the asset it points at.
 * <p>
 * A real entity (rather than a plain join table) so that its delete fires a persistence callback,
 * see {@link DcoLinkAdapter}.
 */
@Entity
public class DcoLink {

  @Id
  @GeneratedValue
  Long id;

  @ManyToOne(optional = false)
  DcoParent parent;

  @OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
  DcoAsset asset;

  public DcoLink(DcoParent parent, DcoAsset asset) {
    this.parent = parent;
    this.asset = asset;
  }

  public Long getId() {
    return id;
  }

  public DcoParent getParent() {
    return parent;
  }

  public DcoAsset getAsset() {
    return asset;
  }
}
