package org.tests.model.draftable;

import io.ebean.annotation.DraftableElement;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * 'Owned' by @Draftable root.
 */
@DraftableElement
@Entity
public class DocumentMedia extends BaseDomain {

  @ManyToOne
  Document document;

  String name;

  String description;

  public Document getDocument() {
    return document;
  }

  public void setDocument(Document document) {
    this.document = document;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
