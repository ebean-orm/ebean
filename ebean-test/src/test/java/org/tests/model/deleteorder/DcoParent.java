package org.tests.model.deleteorder;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

/**
 * Parent of a join entity, see {@link DcoLink}.
 */
@Entity
public class DcoParent {

  @Id
  @GeneratedValue
  Long id;

  String name;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent", orphanRemoval = true)
  List<DcoLink> links = new ArrayList<>();

  public DcoParent(String name) {
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<DcoLink> getLinks() {
    return links;
  }

  public void addAsset(DcoAsset asset) {
    links.add(new DcoLink(this, asset));
  }
}
