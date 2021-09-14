package org.tests.model.history;

import org.tests.model.draftable.BaseDomain;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.List;


@Entity
public class HiDoc extends BaseDomain {

  String name;

  @ManyToMany(mappedBy = "docs")
  List<HiLink> links;

  public HiDoc(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<HiLink> getLinks() {
    return links;
  }

  public void setLinks(List<HiLink> links) {
    this.links = links;
  }
}
