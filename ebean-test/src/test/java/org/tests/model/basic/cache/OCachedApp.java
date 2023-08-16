package org.tests.model.basic.cache;

import io.ebean.annotation.Cache;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Cache(naturalKey = "appName")
@Entity
@Table(name="ocached_app")
@UniqueConstraint(name="uq_ocached_app", columnNames = "app_name")
public class OCachedApp extends OCacheBase {

  private String appName;

  public OCachedApp(String appName) {
    this.appName = appName;
  }

  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }
}
