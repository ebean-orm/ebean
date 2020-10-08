package org.tests.model.basic.first;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@AttributeOverride(name = "name", column = @Column(name = "mod_name"))
@Entity
@Table(name = "f_second")
public class Second extends SuperSecond {

  @Id
  Long id;

  String title;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

}
