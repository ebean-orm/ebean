package org.tests.o2m;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class OmAccountChildDBO {

  @Id
  private long id;

  private final String description;

  @ManyToOne
  private OmAccountDBO bananaRama;

  public OmAccountChildDBO(String description) {
    this.description = description;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public OmAccountDBO getBananaRama() {
    return bananaRama;
  }

  public void setBananaRama(OmAccountDBO bananaRama) {
    this.bananaRama = bananaRama;
  }
}
