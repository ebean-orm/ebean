package org.tests.inheritance.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.List;

@Entity
@DiscriminatorValue("2")
public class GroupConfiguration extends Configuration {
  private String groupName;

  @OneToMany(mappedBy = "groupConfiguration")
  private List<CalculationResult> results;

  public GroupConfiguration() {
    super();
  }

  public GroupConfiguration(String name) {
    super();
    this.groupName = name;
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public List<CalculationResult> getResults() {
    return results;
  }

  public void setResults(List<CalculationResult> results) {
    this.results = results;
  }
}
