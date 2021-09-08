package org.tests.model.nofk;

import io.ebean.annotation.Formula;
import io.ebean.annotation.Identity;
import io.ebean.annotation.Platform;
import io.ebean.annotation.SoftDelete;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

import static io.ebean.annotation.IdentityGenerated.BY_DEFAULT;

@Identity(generated = BY_DEFAULT)
@Entity
public class EUserNoFkSoftDel {

  @Id
  int userId;

  String userName;

  @SoftDelete
  @Formula(select = "${ta}.user_id is null")
  @Formula(select = "CASE WHEN ${ta}.user_id is null THEN 1 ELSE 0 END", platforms = {Platform.SQLSERVER17, Platform.ORACLE})
  // evaluates to true in a left join if bean has been deleted.
  boolean deleted;

  @OneToMany(mappedBy = "ownerSoftDel")
  List<EFileNoFk> files;

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public List<EFileNoFk> getFiles() {
    return files;
  }

  public void setFiles(List<EFileNoFk> files) {
    this.files = files;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

}
