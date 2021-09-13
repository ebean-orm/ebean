package org.tests.lazyloadconf;

import io.ebean.annotation.Cache;

import javax.persistence.*;
import java.util.List;

@Entity
@Cache
@Table(name = "app_config")
public class AppConfig {

  @Id
  @Column(name = "id")
  private Integer id;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "appConfig")
  //@JoinColumn(name = "id", referencedColumnName = "id")
  private List<AppConfigControl> items;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public List<AppConfigControl> getItems() {
    return items;
  }

  public void setItems(List<AppConfigControl> items) {
    this.items = items;
  }
}
