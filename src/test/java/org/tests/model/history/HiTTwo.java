package org.tests.model.history;

import io.ebean.annotation.History;
import org.tests.model.draftable.BaseDomain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@History
@Entity
public class HiTTwo extends BaseDomain {

  String two;

  @OneToMany(cascade = CascadeType.ALL)
  List<HiTThree> threes;

  public HiTTwo(String two) {
    this.two = two;
  }

  public String getTwo() {
    return two;
  }

  public void setTwo(String two) {
    this.two = two;
  }

  public List<HiTThree> getThrees() {
    return threes;
  }

  public void setThrees(List<HiTThree> threes) {
    this.threes = threes;
  }
}
