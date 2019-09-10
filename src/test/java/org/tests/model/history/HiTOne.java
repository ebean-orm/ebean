package org.tests.model.history;

import io.ebean.annotation.History;
import org.tests.model.draftable.BaseDomain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@History
@Entity
public class HiTOne extends BaseDomain {

  String name;

  String comments;

  @OneToMany(cascade = CascadeType.ALL)
  List<HiTTwo> twos;

  public HiTOne(String name) {
    this.name = name;
  }

  public HiTOne() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public List<HiTTwo> getTwos() {
    return twos;
  }

  public void setTwos(List<HiTTwo> twos) {
    this.twos = twos;
  }
}
