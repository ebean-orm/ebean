package org.tests.defaultvalues;

import io.ebean.annotation.Draft;
import io.ebean.annotation.Draftable;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Draftable
public class ReferencedDefaultsModel {

  @Id
  Integer id;

  String name;

  @Draft
  boolean draft;

  public Integer getId() {
    return id;
  }

  public void setId(final Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public boolean isDraft() {
    return draft;
  }

  public void setDraft(final boolean draft) {
    this.draft = draft;
  }
}
