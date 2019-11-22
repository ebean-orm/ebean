package org.tests.o2m;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

@Entity
public class OmBasicChild {

  @Id
  private long id;

  private final String name;

  @ManyToOne
  private final OmBasicParent parent;

  @Version
  private long version;

  public OmBasicChild(String name, OmBasicParent parent) {
    this.name = name;
    this.parent = parent;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public OmBasicParent getParent() {
    return parent;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
