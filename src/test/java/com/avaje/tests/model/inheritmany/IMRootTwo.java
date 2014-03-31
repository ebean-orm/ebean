package com.avaje.tests.model.inheritmany;

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("TWO")
public class IMRootTwo extends IMRoot {

  String title;

  Date whenTitle;
  
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Date getWhenTitle() {
    return whenTitle;
  }

  public void setWhenTitle(Date whenTitle) {
    this.whenTitle = whenTitle;
  }
  
}
