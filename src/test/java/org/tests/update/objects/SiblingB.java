package org.tests.update.objects;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "e_save_test_b")
public class SiblingB {
  @Id
  private Long id;
  @Version
  private long version = 0L;
  @OneToOne(cascade = CascadeType.ALL)
  private SiblingA siblingA;

  private boolean testProperty = false;

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

  public SiblingA getSiblingA() {
    return siblingA;
  }

  public void setSiblingA(SiblingA siblingA) {
    this.siblingA = siblingA;
  }

  public boolean isTestProperty() {
    return testProperty;
  }

  public void setTestProperty(boolean testProperty) {
    this.testProperty = testProperty;
  }
}
