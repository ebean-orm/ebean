package org.tests.merge;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.util.UUID;

@MappedSuperclass
public class MBase {

  @Id @GeneratedValue
  private UUID id;

  @Version
  private long version;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
