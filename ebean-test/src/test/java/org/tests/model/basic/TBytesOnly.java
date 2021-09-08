package org.tests.model.basic;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class TBytesOnly {

  @Id
  Integer id;

  @Lob
  @Basic(fetch = FetchType.EAGER)
  byte[] content;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public byte[] getContent() {
    return content;
  }

  public void setContent(byte[] content) {
    this.content = content;
  }

}
