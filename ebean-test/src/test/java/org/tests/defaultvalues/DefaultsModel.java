package org.tests.defaultvalues;

import io.ebean.annotation.Draft;
import io.ebean.annotation.Draftable;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;

@Entity
@Draftable
public class DefaultsModel {

  @Id
  Integer id;

  @Draft
  boolean draft;

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

  public boolean isDraft() {
    return draft;
  }

  public void setDraft(final boolean draft) {
    this.draft = draft;
  }
}
