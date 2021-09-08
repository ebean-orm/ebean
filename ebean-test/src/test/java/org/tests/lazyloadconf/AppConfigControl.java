package org.tests.lazyloadconf;

import javax.persistence.*;

@Entity
@Table(name = "app_config_control")
public class AppConfigControl {

  @Id
  private Integer id;

  private String name;

  @ManyToOne
  @JoinColumn(name = "config_id", referencedColumnName = "id")
  private AppConfig appConfig;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public AppConfig getAppConfig() {
    return appConfig;
  }

  public void setAppConfig(AppConfig appConfig) {
    this.appConfig = appConfig;
  }
}

