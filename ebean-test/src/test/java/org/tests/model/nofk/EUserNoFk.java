package org.tests.model.nofk;

import io.ebean.annotation.Identity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;

import static io.ebean.annotation.IdentityGenerated.BY_DEFAULT;

@Identity(generated = BY_DEFAULT)
@Entity
public class EUserNoFk {

  @Id
  int userId;

  String userName;

  @OneToMany(mappedBy = "owner")
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

}
