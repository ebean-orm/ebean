package org.tests.model.pview;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pp")
public class Pview {

  @Id
  private UUID id;

  private String name;

  @Basic(optional = false)
  @Column(length = 100, nullable = false)
  private String value;

  @JoinTable(name = "pp_to_ww", joinColumns = {@JoinColumn(name = "pp_id", referencedColumnName = "id")}, inverseJoinColumns = {@JoinColumn(name = "ww_id", referencedColumnName = "id")})
  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Wview> wviews;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public List<Wview> getWviews() {
    return wviews;
  }

  public void setWviews(List<Wview> wviews) {
    this.wviews = wviews;
  }

}
