package org.tests.model.m2m;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Version;
import java.util.List;

@Entity
public class MnyTopic {

  @Id
  Long id;

  String name;

  @Version
  Long version;

  @ManyToMany
  @JoinTable(name = "subtopics",
    joinColumns = @JoinColumn(name = "topic", referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "subtopic", referencedColumnName = "id"))
  List<MnyTopic> subTopics;

  public MnyTopic() {

  }

  public MnyTopic(String name) {
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public List<MnyTopic> getSubTopics() {
    return subTopics;
  }

  public void setSubTopics(List<MnyTopic> subTopics) {
    this.subTopics = subTopics;
  }
}
