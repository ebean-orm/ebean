package org.tests.model.basic;

import javax.persistence.Entity;

import io.ebean.annotation.DbJson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Entity
public class DirtyTestEntity extends BasicDomain {

  private static final long serialVersionUID = 1L;

  private Calendar cal1;

  private String str1;

  @DbJson
  private List<String> lst1 = new ArrayList<>();

  public Calendar getCal1() {
    return cal1;
  }

  public void setCal1(Calendar cal1) {
    this.cal1 = cal1;
  }

  public String getStr1() {
    return str1;
  }

  public void setStr1(String str1) {
    this.str1 = str1;
  }

  public List<String> getLst1() {
    return lst1;
  }

  public void setLst1(List<String> lst1) {
    this.lst1 = lst1;
  }
}
