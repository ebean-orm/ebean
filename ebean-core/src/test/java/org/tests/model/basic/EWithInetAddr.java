package org.tests.model.basic;

import io.ebean.types.Cidr;
import io.ebean.types.Inet;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import java.net.InetAddress;

@Entity
@Table(name = "e_withinet")
public class EWithInetAddr {

  @Id
  Long id;

  @Version
  Long version;

  String name;

  InetAddress inetAddress;

  Inet inet2;

  Cidr cidr;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public InetAddress getInetAddress() {
    return inetAddress;
  }

  public void setInetAddress(InetAddress inetAddress) {
    this.inetAddress = inetAddress;
  }

  public Inet getInet2() {
    return inet2;
  }

  public void setInet2(Inet inet2) {
    this.inet2 = inet2;
  }

  public Cidr getCidr() {
    return cidr;
  }

  public void setCidr(Cidr cidr) {
    this.cidr = cidr;
  }
}
