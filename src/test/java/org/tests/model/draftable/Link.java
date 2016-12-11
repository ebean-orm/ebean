package org.tests.model.draftable;

import io.ebean.annotation.Draft;
import io.ebean.annotation.DraftDirty;
import io.ebean.annotation.DraftReset;
import io.ebean.annotation.Draftable;
import io.ebean.annotation.History;
import io.ebean.annotation.SoftDelete;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.sql.Timestamp;
import java.util.List;


@History
@Draftable
@Entity
public class Link extends BaseDomain {

  @SoftDelete
  boolean deleted;

  String name;

  String location;

  /**
   * Draft reset to null on publish.
   */
  @DraftReset
  Timestamp whenPublish;

  /**
   * Draft reset to null on publish.
   */
  @DraftReset
  @Column(name = "link_comment")
  String comment;

  /**
   * Indicates if the instance is a 'draft' or 'live' bean.
   */
  @Draft
  boolean draft;

  /**
   * Indicates if the draft has modifications that have not been published
   * to 'live'. This is automatically set when a draft is saved.
   */
  @DraftDirty
  boolean dirty;

  @ManyToMany(mappedBy = "links")
  List<Doc> docs;

  public Link(String name) {
    this.name = name;
  }

  public Link() {
  }

  public boolean isDraft() {
    return draft;
  }

  public void setDraft(boolean draft) {
    this.draft = draft;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public List<Doc> getDocs() {
    return docs;
  }

  public void setDocs(List<Doc> docs) {
    this.docs = docs;
  }

  public boolean isDirty() {
    return dirty;
  }

  public void setDirty(boolean dirty) {
    this.dirty = dirty;
  }

  public Timestamp getWhenPublish() {
    return whenPublish;
  }

  public void setWhenPublish(Timestamp whenPublish) {
    this.whenPublish = whenPublish;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }
}
