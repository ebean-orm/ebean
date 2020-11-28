package org.tests.model.history;

import io.ebean.annotation.History;
import io.ebean.annotation.SoftDelete;
import org.tests.model.draftable.BaseDomain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

@History
@Entity
public class HsdUser extends BaseDomain {

  String name;

  @SoftDelete
  boolean deleted;

  @OneToOne(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  HsdSetting setting;

  public HsdUser(String name) {
    this.name = name;
  }

  public HsdUser() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public HsdSetting getSetting() {
    return setting;
  }

  public void setSetting(HsdSetting setting) {
    this.setting = setting;
  }
}
