package org.tests.model.inheritmany;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.util.Date;

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
