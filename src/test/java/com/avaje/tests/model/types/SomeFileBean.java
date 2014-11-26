package com.avaje.tests.model.types;

import javax.persistence.*;
import java.io.File;

@Entity
public class SomeFileBean {

  @Id
  Long id;

  @Version
  Long version;

  String name;

  @Lob
  File file;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public File getFile() {
    return file;
  }

  public void setFile(File file) {
    this.file = file;
  }
}
