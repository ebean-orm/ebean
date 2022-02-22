package org.tests.model.onetoone;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class OtoNotification {

  @Id
  Integer id;

  Integer refId;
  
  // 0 = master, 1 = child
  Integer type;

  String text;
  
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getRefId() {
    return refId;
  }

  public void setRefId(Integer refId) {
    this.refId = refId;
  }

  public Integer getType() {
    return type;
  }

  public void setType(Integer type) {
    this.type = type;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

}
