package org.tests.model.types;

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
  @Column(length = 100 * 1024) // limit to 100kb
  File content;

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

  public File getContent() {
    return content;
  }

  public void setContent(File content) {
    this.content = content;
  }
}
