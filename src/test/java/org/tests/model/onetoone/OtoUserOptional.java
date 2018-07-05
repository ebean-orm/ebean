package org.tests.model.onetoone;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.tests.model.BaseModel;

@Entity
@Table(name = "oto_user_model_optional")
public class OtoUserOptional extends BaseModel {

  private String optional;

  public void setPassword(final String optional) {
    this.optional = optional;
  }

  public String getOptional() {
    return optional;
  }

}
