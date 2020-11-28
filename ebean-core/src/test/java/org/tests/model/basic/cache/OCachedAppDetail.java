package org.tests.model.basic.cache;

import io.ebean.annotation.Cache;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.UniqueConstraint;

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
