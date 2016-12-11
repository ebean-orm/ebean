package org.tests.model.basic;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
public class PFile extends BasicDomain {

  private static final long serialVersionUID = 1L;

  private String name;

  @OneToOne(cascade = CascadeType.ALL)
  private PFileContent fileContent;

  /**
   * Another persistent file.
   */
  @OneToOne(cascade = CascadeType.ALL)
  private PFileContent fileContent2;

  public PFile() {
  }

  public PFile(String name, PFileContent fileContent) {
    super();
    this.name = name;
    this.fileContent = fileContent;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public PFileContent getFileContent() {
    return fileContent;
  }

  public void setFileContent(PFileContent fileContent) {
    this.fileContent = fileContent;
  }

  public PFileContent getFileContent2() {
    return fileContent2;
  }

  public void setFileContent2(PFileContent fileContent2) {
    this.fileContent2 = fileContent2;
  }

}
