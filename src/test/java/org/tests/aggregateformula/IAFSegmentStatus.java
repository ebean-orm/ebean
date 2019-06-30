package org.tests.aggregateformula;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "iaf_segment_status")
public class IAFSegmentStatus {

  @Id
  private long id;

  private String name;

  public IAFSegmentStatus(String name) {
    this.name = name;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
