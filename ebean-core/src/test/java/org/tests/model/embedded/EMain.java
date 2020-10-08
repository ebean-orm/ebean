package org.tests.model.embedded;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "e_main")
public class EMain {

  @Id
  private Integer id;

  private String name;

  @Embedded
  private Eembeddable embeddable = new Eembeddable();

  @Version
  private Long version;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Eembeddable getEmbeddable() {
    return embeddable;
  }

  public void setEmbeddable(Eembeddable embeddable) {
    this.embeddable = embeddable;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

}
