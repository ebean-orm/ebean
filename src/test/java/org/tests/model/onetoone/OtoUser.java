package org.tests.model.onetoone;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.tests.model.BaseModel;

@Entity
@Table(name = "oto_user_model")
public class OtoUser extends BaseModel {

  String name;
  
  @OneToOne(optional = true, cascade = CascadeType.ALL)
  OtoUserOptional userOptional;

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
