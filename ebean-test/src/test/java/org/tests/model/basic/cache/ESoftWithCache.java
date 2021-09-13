package org.tests.model.basic.cache;

import io.ebean.Model;
import io.ebean.annotation.Cache;
import io.ebean.annotation.SoftDelete;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Cache
@Entity
@Table(name = "e_softwithcache")
public class ESoftWithCache extends Model {

  @Id
  long id;

  String name;

  String description;

  @SoftDelete
  boolean deleted;

  @Version
  long version;

  public ESoftWithCache(String name) {
    this.name = name;
  }

  public long id() {
    return id;
  }

  public ESoftWithCache id(long id) {
    this.id = id;
    return this;
  }

  public String name() {
    return name;
  }

  public ESoftWithCache name(String name) {
    this.name = name;
    return this;
  }

  public String description() {
    return description;
  }

  public ESoftWithCache description(String description) {
    this.description = description;
    return this;
  }

  public long version() {
    return version;
  }

  public ESoftWithCache version(long version) {
    this.version = version;
    return this;
  }
}
