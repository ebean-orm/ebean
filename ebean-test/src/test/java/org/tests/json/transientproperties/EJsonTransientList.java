package org.tests.json.transientproperties;

import io.ebean.annotation.Sql;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.List;

@Sql
@Entity
public class EJsonTransientList {

  @Id
  private Long id;

  private String name;

  @Transient
  private Boolean basic;

  @Transient
  private List<String> fileNames;

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

  public List<String> getFileNames() {
    return fileNames;
  }

  public void setFileNames(List<String> fileNames) {
    this.fileNames = fileNames;
  }

}
