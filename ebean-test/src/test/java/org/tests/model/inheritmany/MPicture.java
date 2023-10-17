package org.tests.model.inheritmany;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;


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
