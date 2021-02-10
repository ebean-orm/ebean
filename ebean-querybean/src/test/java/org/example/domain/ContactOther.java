package org.example.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Contact entity bean.
 */
@Entity
@Table(name="be_contact_other")
public class ContactOther extends BaseModel {

  String key;

  int something;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public int getSomething() {
    return something;
  }

  public void setSomething(int something) {
    this.something = something;
  }
}
