package org.tests.model.json;

import io.ebean.annotation.DbJson;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class EBasicJsonString {

  @Id
  long id;

  String title;

  @DbJson
  String content;

  @Version
  long version;

  public EBasicJsonString(String title) {
    this.title = title;
  }

  public long id() {
    return id;
  }

  public EBasicJsonString id(long id) {
    this.id = id;
    return this;
  }

  public String title() {
    return title;
  }

  public EBasicJsonString title(String title) {
    this.title = title;
    return this;
  }

  public String content() {
    return content;
  }

  public EBasicJsonString content(String content) {
    this.content = content;
    return this;
  }

  public long version() {
    return version;
  }

  public EBasicJsonString version(long version) {
    this.version = version;
    return this;
  }
}
