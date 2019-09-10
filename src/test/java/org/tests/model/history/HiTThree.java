package org.tests.model.history;

import io.ebean.annotation.History;
import org.tests.model.draftable.BaseDomain;

import javax.persistence.Entity;

@History
@Entity
public class HiTThree extends BaseDomain {

  String three;

  public HiTThree(String three) {
    this.three = three;
  }

  public HiTThree() {
  }

  public String getThree() {
    return three;
  }

  public void setThree(String three) {
    this.three = three;
  }
}
