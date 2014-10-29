package com.avaje.tests.model.info;

import com.avaje.ebean.Model;

import javax.persistence.*;

@Entity
public class InfoCustomer extends Model {

  public static Finder<Long,InfoCustomer> find = new Finder<Long,InfoCustomer>(Long.class, InfoCustomer.class);

  @Id
  Long id;

  @Version
  Long version;

  String name;

  @OneToOne(cascade = CascadeType.ALL)
  //@JoinColumn(name = "id_info_company")
  InfoCompany infos = new InfoCompany();

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

  public InfoCompany getInfos() {
    return infos;
  }

  public void setInfos(InfoCompany infos) {
    this.infos = infos;
  }
}
