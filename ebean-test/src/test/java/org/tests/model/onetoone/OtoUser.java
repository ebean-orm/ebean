package org.tests.model.onetoone;

import org.tests.model.BaseModel;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "oto_user_model")
public class OtoUser extends BaseModel {

  private String name;

  @OneToOne(cascade = CascadeType.ALL)
  private OtoUserOptional userOptional;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setOptional(OtoUserOptional userOptional) {
    this.userOptional = userOptional;
  }

}
