package com.avaje.tests.model.softdelete;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class ESoftDelTop extends BaseSoftDelete {

  String top;

  @OneToMany(mappedBy = "top", cascade = CascadeType.ALL)
  List<ESoftDelMid> mids;

  public ESoftDelTop(String top) {
    this.top = top;
  }

  public String getTop() {
    return top;
  }

  public void setTop(String top) {
    this.top = top;
  }

  public List<ESoftDelMid> getMids() {
    return mids;
  }

  public void setMids(List<ESoftDelMid> mids) {
    this.mids = mids;
  }

  public ESoftDelMid addMids(String mid) {

    ESoftDelMid bean = new ESoftDelMid(this, mid);
    getMids().add(bean);
    return bean;
  }

}
