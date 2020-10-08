package org.tests.model.zero;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

@Entity
public class WithZero {

  @Id
  long id = 0;

  String name;

  @ManyToOne
  WithZeroParent parent;

  @Column(columnDefinition = "varchar(2) default 'en' not null")
  String lang = "en";

  @Version
  long version = 0;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLang() {
    return lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  public WithZeroParent getParent() {
    return parent;
  }

  public void setParent(WithZeroParent parent) {
    this.parent = parent;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
