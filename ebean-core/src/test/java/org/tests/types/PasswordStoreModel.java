package org.tests.types;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.validation.constraints.Size;

import org.tests.model.BaseModel;

@Entity
public class PasswordStoreModel extends BaseModel {
  private static final long serialVersionUID = 1L;
  // PasswordStoreModel should have the following column definitions in DDL
  // enc1 varchar(30),
  // enc2 varchar(40),
  // enc3 clob,
  // enc4 varbinary(30),
  // enc5 varbinary(40),
  // enc6 blob,

  @Size(max = 30)
  private EncryptedString enc1;
  @Column(length = 40)
  private EncryptedString enc2;
  @Lob
  private EncryptedString enc3;

  @Size(max = 30)
  private EncryptedBinary enc4;
  @Column(length = 40)
  private EncryptedBinary enc5;
  @Lob
  private EncryptedBinary enc6;

  public EncryptedString getEnc1() {
    return enc1;
  }

  public void setEnc1(EncryptedString enc1) {
    this.enc1 = enc1;
  }

  public EncryptedString getEnc2() {
    return enc2;
  }

  public void setEnc2(EncryptedString enc2) {
    this.enc2 = enc2;
  }

  public EncryptedString getEnc3() {
    return enc3;
  }

  public void setEnc3(EncryptedString enc3) {
    this.enc3 = enc3;
  }

  public EncryptedBinary getEnc4() {
    return enc4;
  }

  public void setEnc4(EncryptedBinary enc4) {
    this.enc4 = enc4;
  }

  public EncryptedBinary getEnc5() {
    return enc5;
  }

  public void setEnc5(EncryptedBinary enc5) {
    this.enc5 = enc5;
  }

  public EncryptedBinary getEnc6() {
    return enc6;
  }

  public void setEnc6(EncryptedBinary enc6) {
    this.enc6 = enc6;
  }

}
