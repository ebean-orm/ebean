package org.tests.model.basic;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToOne;

@Entity
public class PersistentFileContent extends BasicDomain {

  private static final long serialVersionUID = 1L;

  /**
   * The persistent file.
   */
  @OneToOne(cascade = CascadeType.ALL)
  private PersistentFile persistentFile;

  /**
   * The content.
   */
  @Lob
  private byte[] content;

  public PersistentFileContent() {
  }

  public PersistentFileContent(byte[] content) {
    super();
    this.content = content;
  }

  public PersistentFile getPersistentFile() {
    return persistentFile;
  }

  public void setPersistentFile(PersistentFile persistentFile) {
    this.persistentFile = persistentFile;
  }

  public byte[] getContent() {
    return content;
  }

  public void setContent(byte[] content) {
    this.content = content;
  }
}
