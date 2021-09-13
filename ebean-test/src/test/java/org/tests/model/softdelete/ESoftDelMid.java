package org.tests.model.softdelete;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class ESoftDelMid extends BaseSoftDelete {

  @ManyToOne
  ESoftDelTop top;

  String mid;

  @ManyToOne(cascade = CascadeType.ALL)
  ESoftDelUp up;

  @OneToMany(cascade = CascadeType.ALL)
  List<ESoftDelDown> downs;

  public ESoftDelMid(ESoftDelTop top, String mid) {
    this.top = top;
    this.mid = mid;
  }

  public ESoftDelTop getTop() {
    return top;
  }

  public void setTop(ESoftDelTop top) {
    this.top = top;
  }

  public ESoftDelUp getUp() {
    return up;
  }

  public void setUp(ESoftDelUp up) {
    this.up = up;
  }

  public String getMid() {
    return mid;
  }

  public void setMid(String mid) {
    this.mid = mid;
  }

  public List<ESoftDelDown> getDowns() {
    return downs;
  }

  public void setDowns(List<ESoftDelDown> downs) {
    this.downs = downs;
  }

  public void addDown(String down) {
    getDowns().add(new ESoftDelDown(down));
  }
}
