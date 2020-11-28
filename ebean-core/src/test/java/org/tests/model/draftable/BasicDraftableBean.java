package org.tests.model.draftable;

import io.ebean.annotation.Draft;
import io.ebean.annotation.Draftable;

import javax.persistence.Entity;

@Entity
@Draftable
public class BasicDraftableBean extends BaseDomain {

  private String name;

  @Draft
  boolean draft;

  public BasicDraftableBean(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isDraft() {
    return draft;
  }
}
