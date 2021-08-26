package org.tests.model.controller;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Jonas P&ouml;hler, FOCONIS AG
 */
@Entity
public class SoftRefB {

  @Id
  private Integer id;

  private String title;

  public Integer getId() {
    return id;
  }

  public void setId(final Integer id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(final String title) {
    this.title = title;
  }
}
