package org.tests.update.objects;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "e_save_test_a")
public class SiblingA {
  @Id
  private Long id;
  @Version
  private long version = 0L;
  @OneToOne(cascade = CascadeType.ALL, mappedBy = "siblingA")
  private SiblingB siblingB;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public SiblingB getSiblingB() {
    return siblingB;
  }

  public void setSiblingB(SiblingB siblingB) {
    this.siblingB = siblingB;
  }
}
