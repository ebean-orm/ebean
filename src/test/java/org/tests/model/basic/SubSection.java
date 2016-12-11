package org.tests.model.basic;

import io.ebean.annotation.Cache;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Cache
@Entity
public class SubSection extends BasicDomain {

  private static final long serialVersionUID = 1L;

  @ManyToOne
  private Section section;

  private String title;

  public SubSection() {
  }

  public SubSection(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

}
