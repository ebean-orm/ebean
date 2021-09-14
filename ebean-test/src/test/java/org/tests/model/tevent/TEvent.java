package org.tests.model.tevent;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Version;

@Entity
public class TEvent {

  @Id
  Long id;

  String name;

  @OneToOne(mappedBy = "event")
  TEventOne one;

  @Version
  Long version;

  public TEvent(String name) {
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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
