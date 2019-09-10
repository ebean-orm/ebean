package org.tests.update.objects;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "e_save_test_d")
public class Child {
  @Id
  private Long id;
  @Version
  private long version = 0L;
  @OneToOne
  private Parent parent;

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

  public Parent getParent() {
    return parent;
  }

  public void setParent(Parent parent) {
    this.parent = parent;
  }

  public boolean isTestProperty() {
    return testProperty;
  }

  public void setTestProperty(boolean testProperty) {
    this.testProperty = testProperty;
  }
}
