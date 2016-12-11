package org.tests.model.history;

import org.tests.model.draftable.BaseDomain;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.List;


@Entity
public class HeDoc extends BaseDomain {

  String name;

  @ManyToMany(mappedBy = "docs")
  List<HeLink> links;

  public HeDoc(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<HeLink> getLinks() {
    return links;
  }

  public void setLinks(List<HeLink> links) {
    this.links = links;
  }
}
