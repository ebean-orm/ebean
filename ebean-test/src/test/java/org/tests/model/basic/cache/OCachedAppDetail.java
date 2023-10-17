package org.tests.model.basic.cache;

import io.ebean.annotation.Cache;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.UniqueConstraint;

@Cache(naturalKey = {"app", "detail"})
@Entity
@UniqueConstraint(columnNames = {"app_id", "detail"})
public class OCachedAppDetail extends OCacheBase {

  @ManyToOne(optional = false)
  private final OCachedApp app;

  private final String detail;

  public OCachedAppDetail(OCachedApp app, String detail) {
    this.app = app;
    this.detail = detail;
  }

  public OCachedApp getApp() {
    return app;
  }

  public String getDetail() {
    return detail;
  }
}
