package org.tests.model.basic.cache;

import io.ebean.annotation.Cache;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Cache(naturalKey = "appName")
@Entity
@Table(name = "ocached_app")
@UniqueConstraint(name = "uq_ocached_app", columnNames = "app_name")
public class OCachedAppCustom extends OCachedApp {

  String custom;

  public OCachedAppCustom(String appName) {
    super(appName);
  }

  public String getCustom() {
    return custom;
  }

  public void setCustom(String custom) {
    this.custom = custom;
  }
}
