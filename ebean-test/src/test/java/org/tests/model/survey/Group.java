package org.tests.model.survey;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "survey_group")
public class Group {
  @Id
  public Long id;

  String name;

  public Group(String name) {
    this.name = name;
  }

  @ManyToOne
  @JoinColumn(name = "categoryobjectid")
  private Category category;

  @OneToMany(mappedBy = "group", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @OrderBy("sequenceNumber")
  private List<Question> questions = new ArrayList<>();

  private int sequenceNumber;

  public List<Question> getQuestions() {
    return questions;
  }

  public void setQuestions(List<Question> questions) {
    this.questions = questions;
  }

  public void setSequenceNumber(int number) {
    this.sequenceNumber = number;
  }

  public int getSequenceNumber() {
    return sequenceNumber;
  }
}
