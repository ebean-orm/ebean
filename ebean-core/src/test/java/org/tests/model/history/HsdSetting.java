package org.tests.model.history;

import io.ebean.annotation.History;
import io.ebean.annotation.SoftDelete;
import org.tests.model.draftable.BaseDomain;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

@History
@Entity
public class HsdSetting extends BaseDomain {

  String code;
  String content;

  @SoftDelete
  boolean deleted;

  @OneToOne
  private HsdUser user;


  public HsdSetting(String code) {
    this.code = code;
  }

  public HsdSetting() {
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public HsdUser getUser() {
    return user;
  }

  public void setUser(HsdUser user) {
    this.user = user;
  }
}
