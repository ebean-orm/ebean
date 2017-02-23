package org.tests.model.softdelete;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class ESoftDelTop extends BaseSoftDelete {

  String name;

  @OneToMany(mappedBy = "top", cascade = CascadeType.ALL)
  List<ESoftDelMid> mids;

  public ESoftDelTop(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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
