package org.tests.model.basic;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
public class PersistentFile extends BasicDomain {

  private static final long serialVersionUID = 1L;

  private String name;

  @OneToOne(mappedBy = "persistentFile", cascade = CascadeType.ALL)
  private PersistentFileContent persistentFileContent;

  public PersistentFile() {
  }

  public PersistentFile(String name,
                        PersistentFileContent persistentFileContent) {
    super();
    this.name = name;
    this.persistentFileContent = persistentFileContent;

    this.persistentFileContent.setPersistentFile(this);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public PersistentFileContent getPersistentFileContent() {
    return persistentFileContent;
  }

  public void setPersistentFileContent(PersistentFileContent persistentFileContent) {
    this.persistentFileContent = persistentFileContent;
  }

}
