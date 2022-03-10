package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

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

  @OneToMany
  private List<EBasicClobNoVerChild> children = new ArrayList<>();

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

  public List<EBasicClobNoVerChild> children() {
    return children;
  }

  public EBasicClobNoVer children(List<EBasicClobNoVerChild> children) {
    this.children = children;
    return this;
  }
}
