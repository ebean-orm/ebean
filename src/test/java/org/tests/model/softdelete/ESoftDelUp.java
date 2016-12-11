package org.tests.model.softdelete;

import javax.persistence.Entity;

@Entity
public class ESoftDelUp extends BaseSoftDelete {

  String up;

  public ESoftDelUp(String up) {
    this.up = up;
  }

  public String getUp() {
    return up;
  }

  public void setUp(String up) {
    this.up = up;
  }
}
