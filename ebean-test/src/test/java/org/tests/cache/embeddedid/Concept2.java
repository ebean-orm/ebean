package org.tests.cache.embeddedid;

import javax.persistence.*;

@Entity
@IdClass(ConceptId.class)
@SuppressWarnings("unused")
public class Concept2 {

  @Id
  @Column(name = "conn_id", nullable = false)
  private String id;

  @Id
  @Column(name = "conn_network_id", nullable = false)
  private String networkId;

  private String label;


  public Concept2(String networkId, String id, String label) {
    this.networkId = networkId;
    this.id = id;
    this.label = label;
  }

  public String id() {
    return id;
  }

  public String networkId() {
    return networkId;
  }

  public String label() {
    return label;
  }

}
