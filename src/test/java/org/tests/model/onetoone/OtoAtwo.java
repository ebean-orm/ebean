package org.tests.model.onetoone;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class OtoAtwo {

  @Id
  private String id;

  private String description;

  @OneToOne(orphanRemoval=true, cascade = CascadeType.ALL)
  private OtoAone aone;

  public OtoAtwo(String id, String description){
    this.id = id;
    this.description = description;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public OtoAone getAone() {
    return aone;
  }

  public void setAone(OtoAone aone) {
    this.aone = aone;
  }
}
