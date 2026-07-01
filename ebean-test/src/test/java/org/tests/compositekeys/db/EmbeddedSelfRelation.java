package org.tests.compositekeys.db;

import jakarta.persistence.*;

@Entity
@Table(name = "em_transactions")
public class EmbeddedSelfRelation {

  @EmbeddedId
  private final PartitionKey key;

  @ManyToOne
  @JoinColumns({
    @JoinColumn(name = "org_id", referencedColumnName = "org_id", insertable = false, updatable = false),
    @JoinColumn(name = "root_code", referencedColumnName = "code"),
  })
  private EmbeddedSelfRelation root;

  public EmbeddedSelfRelation(PartitionKey key) {
    this.key = key;
  }

  public PartitionKey key() {
    return key;
  }

  public EmbeddedSelfRelation root() {
    return root;
  }

  public void setRoot(EmbeddedSelfRelation root) {
    this.root = root;
  }
}

