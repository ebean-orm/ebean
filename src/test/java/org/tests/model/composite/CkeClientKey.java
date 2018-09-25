package org.tests.model.composite;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class CkeClientKey {

  @Basic(optional = false)
  @Column(name = "cod_cpny")
  private int codCompany;

  @Basic(optional = false)
  @Column(name = "cod_client")
  private String codClient;

  public CkeClientKey(int codCompany, String codClient) {
    this.codCompany = codCompany;
    this.codClient = codClient;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CkeClientKey that = (CkeClientKey) o;
    return codCompany == that.codCompany &&
      Objects.equals(codClient, that.codClient);
  }

  @Override
  public int hashCode() {
    return Objects.hash(codCompany, codClient);
  }

  public int getCodCompany() {
    return codCompany;
  }

  public void setCodCompany(int codCompany) {
    this.codCompany = codCompany;
  }

  public String getCodClient() {
    return codClient;
  }

  public void setCodClient(String codClient) {
    this.codClient = codClient;
  }
}
