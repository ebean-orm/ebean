package org.tests.model.json;

import io.ebean.annotation.DbJsonB;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class EBasicJsonBString {

  @Id
  long id;

  String title;

  @DbJsonB
  String content;

  @Version
  long version;

  public EBasicJsonBString(String title) {
    this.title = title;
  }

  public long id() {
    return id;
  }

  public EBasicJsonBString id(long id) {
    this.id = id;
    return this;
  }

  public String title() {
    return title;
  }

  public EBasicJsonBString title(String title) {
    this.title = title;
    return this;
  }

  public String content() {
    return content;
  }

  public EBasicJsonBString content(String content) {
    this.content = content;
    return this;
  }

  public long version() {
    return version;
  }

  public EBasicJsonBString version(long version) {
    this.version = version;
    return this;
  }
}
