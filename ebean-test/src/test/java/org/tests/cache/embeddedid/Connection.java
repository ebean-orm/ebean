package org.tests.cache.embeddedid;

import javax.persistence.*;

@Entity
@IdClass(ConceptId.class)
@SuppressWarnings("unused")
public class Connection {
  @Id
  private String id;

  @Id
  private String networkId;

  private String label;

  @ManyToOne(optional = false)
  @JoinColumns({
    @JoinColumn(name = "from_conc", referencedColumnName = "id", nullable = false),
    @JoinColumn(
      name = "network_id", referencedColumnName = "network_id",
      nullable = false, insertable = false, updatable = false
    )
  })
  private Concept from;

  @ManyToOne(optional = false)
  @JoinColumns({
    @JoinColumn(name = "to_conc", referencedColumnName = "id", nullable = false),
    @JoinColumn(
      name = "network_id", referencedColumnName = "network_id",
      nullable = false, insertable = false, updatable = false
    )
  })
  private Concept to;

  public Connection(
    String networkId, String id, String label,
    Concept from, Concept to
  ) {
    this.networkId = networkId;
    this.id = id;
    this.label = label;
    this.from = from;
    this.to = to;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public Concept from() {
    return from;
  }

  public void setFrom(Concept from) {
    this.from = from;
  }

  public Concept to() {
    return to;
  }

  public void setTo(Concept to) {
    this.to = to;
  }
}
