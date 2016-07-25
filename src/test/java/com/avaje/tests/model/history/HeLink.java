package com.avaje.tests.model.history;

import com.avaje.ebean.annotation.History;
import com.avaje.ebean.annotation.HistoryExclude;
import com.avaje.tests.model.draftable.BaseDomain;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.List;

@History
@Entity
public class HeLink extends BaseDomain {

  String name;

  String location;

  String comment;

  @HistoryExclude
  @ManyToMany
  List<HeDoc> docs;

  public HeLink(String name, String location) {
    this.name = name;
    this.location = location;
  }

  public HeLink() {
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

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public List<HeDoc> getDocs() {
    return docs;
  }

  public void setDocs(List<HeDoc> docs) {
    this.docs = docs;
  }
}
