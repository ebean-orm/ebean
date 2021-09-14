package org.tests.model.draftable;

import io.ebean.annotation.Draftable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.List;


@Draftable
@Entity
public class Doc extends BaseDomain {

  String name;

  @ManyToMany(cascade = CascadeType.ALL)
  List<Link> links;

  public Doc(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Link> getLinks() {
    return links;
  }

  public void setLinks(List<Link> links) {
    this.links = links;
  }
}
