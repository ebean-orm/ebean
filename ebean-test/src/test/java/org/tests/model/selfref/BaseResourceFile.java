package org.tests.model.selfref;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.UUID;

@MappedSuperclass
public abstract class BaseResourceFile implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "id", nullable = false, length = 64)
  private String id;

  public BaseResourceFile() {
    this.id = newId();
  }

  public String getId() {
    return id;
  }

  protected void setId(String id) {
    this.id = id;
  }

  protected String newId() {
    return UUID.randomUUID().toString();
  }
}
