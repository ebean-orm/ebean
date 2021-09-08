package org.tests.model.composite;

import javax.persistence.*;

@Entity
public class CkeClient {

  @EmbeddedId
  private CkeClientKey clientPK;

  @JoinColumns({
    @JoinColumn(name = "username", referencedColumnName = "username"),
    @JoinColumn(name = "cod_cpny", referencedColumnName = "cod_cpny", insertable = false, updatable = false)
    })
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private CkeUser user;

  private String notes;

  public CkeUser getUser() {
    return user;
  }

  public void setUser(CkeUser user) {
    this.user = user;
  }

  public CkeClientKey getClientPK() {
    return clientPK;
  }

  public void setClientPK(CkeClientKey clientPK) {
    this.clientPK = clientPK;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }
}
