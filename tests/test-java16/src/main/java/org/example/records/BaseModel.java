package org.example.records;

import io.ebean.Model;
import io.ebean.annotation.Identity;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

@Identity(start = 1000, cache = 100)
@MappedSuperclass
public class BaseModel extends Model {

  @Id
  long id;

  @Version
  long version;

  public long id() {
    return id;
  }

  public void id(long id) {
    this.id = id;
  }

  public long version() {
    return version;
  }

  public void version(long version) {
    this.version = version;
  }
}
