package org.tests.model.inheritmany;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;


@Entity
@DiscriminatorValue("Picture")
public class MPicture extends MMedia {


  String note;

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

}
