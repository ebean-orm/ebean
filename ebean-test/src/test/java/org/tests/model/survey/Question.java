package org.tests.model.survey;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Question {
  @Id
  public Long id;

  String name;

  public Question(String name) {
    this.name = name;
  }

  @ManyToOne
  @JoinColumn(name = "groupobjectid")
  private Group group;

  private int sequenceNumber;

  public void setSequenceNumber(int number) {
    this.sequenceNumber = number;
  }

  public int getSequenceNumber() {
    return sequenceNumber;
  }
}
