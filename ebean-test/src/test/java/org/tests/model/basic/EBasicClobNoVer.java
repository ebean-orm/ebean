package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class EBasicClobNoVer {

  @Id
  private Long id;

  private String name;

  /**
   * Note that lobs default to FetchType.LAZY - see EBasicClobFetchEager.
   */
  @Lob
  private String description;

  public void setId(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
