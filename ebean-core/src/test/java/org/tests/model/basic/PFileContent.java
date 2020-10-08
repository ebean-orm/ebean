package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
public class PFileContent extends BasicDomain {

  private static final long serialVersionUID = 1L;

  /**
   * The content.
   */
  @Lob
  private byte[] content;

  public PFileContent() {
  }

  public PFileContent(byte[] content) {
    super();
    this.content = content;
  }

  public byte[] getContent() {
    return content;
  }

  public void setContent(byte[] content) {
    this.content = content;
  }
}
