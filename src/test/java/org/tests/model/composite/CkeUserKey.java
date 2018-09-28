package org.tests.model.composite;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class CkeUserKey {

  @Basic(optional = false)
  @Column(name = "username")
  private String username;

  @Basic(optional = false)
  @Column(name = "cod_cpny")
  private int codCompany;

  public CkeUserKey(int codCompany, String username) {
    this.codCompany = codCompany;
    this.username = username;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CkeUserKey that = (CkeUserKey) o;
    return codCompany == that.codCompany &&
      Objects.equals(username, that.username);
  }

  @Override
  public int hashCode() {
    return Objects.hash(codCompany, username);
  }

  public int getCodCompany() {
    return codCompany;
  }

  public void setCodCompany(int codCompany) {
    this.codCompany = codCompany;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
