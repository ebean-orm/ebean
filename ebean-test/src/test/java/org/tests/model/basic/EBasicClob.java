package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Version;
import java.sql.Timestamp;

@Entity
public class EBasicClob {

  @Id
  private Long id;

  private String name;

  private String title;

  @Lob
  private String description;

  @Version
  private Timestamp lastUpdate;


  public void setId(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Timestamp getLastUpdate() {
    return lastUpdate;
  }

  public void setLastUpdate(Timestamp lastUpdate) {
    this.lastUpdate = lastUpdate;
  }

}
