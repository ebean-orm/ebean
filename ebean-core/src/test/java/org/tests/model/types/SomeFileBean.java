package org.tests.model.types;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Version;
import java.io.File;

@Entity
public class SomeFileBean {

  @Id
  Long id;

  @Version
  Long version;

  String name;

  @Lob
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
