package org.tests.model.history;

import io.ebean.annotation.History;
import io.ebean.annotation.SoftDelete;
import org.tests.model.draftable.BaseDomain;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

@History
@Entity
public class HsdSetting extends BaseDomain {

  String key;
  String val;

  @SoftDelete
  boolean deleted;

  @OneToOne
  private HsdUser user;


  public HsdSetting(String key) {
    this.key = key;
  }

  public HsdSetting() {
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getVal() {
    return val;
  }

  public void setVal(String val) {
    this.val = val;
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
