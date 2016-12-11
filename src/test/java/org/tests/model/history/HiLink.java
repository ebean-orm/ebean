package org.tests.model.history;

import io.ebean.annotation.History;
import org.tests.model.draftable.BaseDomain;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.List;

@History
@Entity
public class HiLink extends BaseDomain {

  String name;

  String location;

  String comments;

  @ManyToMany
  List<HiDoc> docs;

  public HiLink(String name, String location) {
    this.name = name;
    this.location = location;
  }

  public HiLink() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public List<HiDoc> getDocs() {
    return docs;
  }

  public void setDocs(List<HiDoc> docs) {
    this.docs = docs;
  }
}
