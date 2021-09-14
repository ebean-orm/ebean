package org.tests.lazyloadconf;

import io.ebean.annotation.Cache;

import javax.persistence.*;

@Entity
@Cache(enableBeanCache = true)
@Table(name="app_config_re")
public class Relationship {

  @Id
  private Integer id;

  @ManyToOne
  @JoinColumn(name="config_id",referencedColumnName="id")
  private AppConfig appConfig;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public AppConfig getAppConfig() {
    return appConfig;
  }

  public void setAppConfig(AppConfig appConfig) {
    this.appConfig = appConfig;
  }
}

