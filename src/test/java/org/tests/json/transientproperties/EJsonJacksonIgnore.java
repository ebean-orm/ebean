package org.tests.json.transientproperties;

import io.ebean.annotation.Sql;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.Id;

@Sql
@Entity
public class EJsonJacksonIgnore {

  @Id
  private Long id;

  private String name;

  @JsonIgnore
  private Boolean basic;

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

  public Boolean getBasic() {
    return basic;
  }

  public void setBasic(Boolean basic) {
    this.basic = basic;
  }
}
