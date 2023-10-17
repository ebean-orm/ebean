package org.tests.model.onetoone;

import org.tests.model.BaseModel;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

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
