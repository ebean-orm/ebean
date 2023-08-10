package org.tests.cache.embeddedid;

import javax.persistence.*;
import java.util.List;

@Entity
@IdClass(ConceptId.class)
@SuppressWarnings("unused")
public class Concept {
  @Id
  private String id;

  @Id
  private String networkId;

  private String label;

  @OneToMany(mappedBy = "from", cascade = {CascadeType.ALL})
  private List<Connection> outgoingConnections;

  @OneToMany(mappedBy = "to", cascade = {CascadeType.ALL})
  private List<Connection> incomingConnections;

  public Concept(String networkId, String id, String label) {
    this.networkId = networkId;
    this.id = id;
    this.label = label;
  }

  public String id() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String networkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public String label() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }
}
