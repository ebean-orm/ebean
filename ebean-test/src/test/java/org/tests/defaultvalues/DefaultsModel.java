package org.tests.defaultvalues;

import io.ebean.annotation.Draft;
import io.ebean.annotation.Draftable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class DefaultsModel {

  @Id
  Integer id;


  @OneToMany(cascade = CascadeType.ALL)
  List<ReferencedDefaultsModel> relatedModels;

  public Integer getId() {
    return id;
  }

  public void setId(final Integer id) {
    this.id = id;
  }

  public List<ReferencedDefaultsModel> getRelatedModels() {
    return relatedModels;
  }

  public void setRelatedModels(final List<ReferencedDefaultsModel> relatedModels) {
    this.relatedModels = relatedModels;
  }
}
