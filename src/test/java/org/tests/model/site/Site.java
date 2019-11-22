package org.tests.model.site;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class Site {

  @Id
  private UUID id;

  private String name;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent")
  private List<Site> children = new ArrayList<>();

  @ManyToOne(cascade = {})
  private Site parent;

  @OneToOne(cascade = CascadeType.ALL)
  private DataContainer dataContainer;

  @OneToOne(cascade = CascadeType.ALL)
  private SiteAddress siteAddress;

  public UUID getId() {
    return id;
  }

  public void setId(final UUID id) {
    this.id = id;
  }

  public List<Site> getChildren() {
    return children;
  }

  public void setChildren(final List<Site> children) {
    this.children = children;
  }

  public Site getParent() {
    return parent;
  }

  public void setParent(final Site parent) {
    this.parent = parent;
  }

  public SiteAddress getSiteAddress() {
    return siteAddress;
  }

  public void setSiteAddress(final SiteAddress siteAddress) {
    this.siteAddress = siteAddress;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public DataContainer getDataContainer() {
    return dataContainer;
  }

  public void setDataContainer(final DataContainer dataContainer) {
    this.dataContainer = dataContainer;
  }
}
