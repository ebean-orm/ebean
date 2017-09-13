package org.tests.model.survey;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Question {
  @Id
  public Long id;

  String name;

  public Question(String name) {
    this.name = name;
  }

  @ManyToOne
  @JoinColumn(name = "groupObjectId")
  private Group group;

  private int sequenceNumber;

  public void setSequenceNumber(int number) {
    this.sequenceNumber = number;
  }

  public int getSequenceNumber() {
    return sequenceNumber;
  }
}
