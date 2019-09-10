package org.multitenant.partition;

import javax.persistence.Entity;

@Entity
public class MtContent extends MtTenantAware {

  String title;

  String body;

  public MtContent(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

}
