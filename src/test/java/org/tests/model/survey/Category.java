package org.tests.model.survey;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import java.util.List;

@Entity
public class Category {
  @Id
  public Long id;

  String name;

  public Category(String name) {
    this.name = name;
  }

  @ManyToOne
  @JoinColumn(name = "surveyObjectId")
  private Survey survey;

  @OneToMany(mappedBy = "category", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @OrderBy("sequenceNumber")
  private List<Group> groups;

  private int sequenceNumber;

  public List<Group> getGroups() {
    return groups;
  }

  public void setGroups(List<Group> groups) {
    this.groups = groups;
  }

  public void setSequenceNumber(int number) {
    this.sequenceNumber = number;
  }
  
  public int getSequenceNumber() {
    return sequenceNumber;
  }
}
