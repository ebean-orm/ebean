package com.avaje.tests.model.tevent;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Version;
import java.util.List;

@Entity
public class TEventOne {

  @Id
  Long id;

  String one;

  @Version
  Long version;

  @OneToOne
  TEvent event;

  @OneToMany(mappedBy = "one")
  List<TEventMany> many;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getOne() {
    return one;
  }

  public void setOne(String one) {
    this.one = one;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public TEvent getEvent() {
    return event;
  }

  public void setEvent(TEvent event) {
    this.event = event;
  }

  public List<TEventMany> getMany() {
    return many;
  }

  public void setMany(List<TEventMany> many) {
    this.many = many;
  }
}
