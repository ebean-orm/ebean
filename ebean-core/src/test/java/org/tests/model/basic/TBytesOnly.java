package org.tests.model.basic;

import javax.persistence.*;

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
