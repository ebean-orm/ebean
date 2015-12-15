package com.avaje.tests.model.draftable;

import com.avaje.ebean.annotation.DraftOnly;
import com.avaje.ebean.annotation.Draftable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.sql.Timestamp;
import java.util.List;


@Draftable
@Entity
public class Document extends BaseDomain {

  @Column(unique = true)
  String title;

  String body;

  @DraftOnly
  Timestamp whenPublish;

  @ManyToOne
  Organisation organisation;

  /**
   * Relationship to draftable elements.
   */
  //@PrivateOwned
  @OneToMany(mappedBy = "document")//, cascade = CascadeType.ALL)
  List<DocumentMedia> media;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public Organisation getOrganisation() {
    return organisation;
  }

  public void setOrganisation(Organisation organisation) {
    this.organisation = organisation;
  }

  public List<DocumentMedia> getMedia() {
    return media;
  }

  public void setMedia(List<DocumentMedia> media) {
    this.media = media;
  }

  public Timestamp getWhenPublish() {
    return whenPublish;
  }

  public void setWhenPublish(Timestamp whenPublish) {
    this.whenPublish = whenPublish;
  }
}
