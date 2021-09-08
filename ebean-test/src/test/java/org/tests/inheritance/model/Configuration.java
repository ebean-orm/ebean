package org.tests.inheritance.model;

import io.ebean.annotation.Cache;
import io.ebean.annotation.ChangeLog;

import javax.persistence.*;

@ChangeLog
@Entity
@Cache(enableQueryCache = true)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING, columnDefinition = "varchar(21)")
public class Configuration extends AbstractBaseClass {
  
  @Id
  @Column(name = "id")
  private Integer id;


  @ManyToOne
  private Configurations configurations;


  public Configuration() {
    super();
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Configurations getConfigurations() {
    return configurations;
  }

  public void setConfigurations(Configurations configurations) {
    this.configurations = configurations;
  }
}
