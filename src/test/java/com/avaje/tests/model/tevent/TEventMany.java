package com.avaje.tests.model.tevent;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

@Entity
public class TEventMany {

  @Id
  Long id;

  String many;

  @ManyToOne
  TEventOne one;

  @Version
  Long version;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getMany() {
    return many;
  }

  public void setMany(String many) {
    this.many = many;
  }

  public TEventOne getOne() {
    return one;
  }

  public void setOne(TEventOne one) {
    this.one = one;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}
