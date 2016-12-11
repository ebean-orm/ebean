package org.tests.model.composite;


import javax.persistence.Embeddable;

@Embeddable
public class RCustomerKey {
  private String company;
  private String name;

  public RCustomerKey() {
  }

  public RCustomerKey(String company, String name) {
    this.company = company;
    this.name = name;
  }

  public String getCompany() {
    return company;
  }

  public void setCompany(String company) {
    this.company = company;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final RCustomerKey other = (RCustomerKey) obj;
    if ((this.company == null) ? (other.company != null) : !this.company.equals(other.company)) {
      return false;
    }
    if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 89 * hash + (this.company != null ? this.company.hashCode() : 0);
    hash = 89 * hash + (this.name != null ? this.name.hashCode() : 0);
    return hash;
  }


}

