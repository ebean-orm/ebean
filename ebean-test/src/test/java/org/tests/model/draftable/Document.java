package org.tests.model.draftable;

import io.ebean.Finder;
import io.ebean.annotation.Draft;
import io.ebean.annotation.DraftOnly;
import io.ebean.annotation.Draftable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.List;


@Draftable
@Entity
public class Document extends BaseDomain {

  public static DocumentFinder find = new DocumentFinder();

  @Column(unique = true)
  @Size(max=127)
  String title;

  String body;

  @Draft
  boolean draft;

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

  public boolean isDraft() {
    return draft;
  }

  public void setDraft(boolean draft) {
    this.draft = draft;
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

  public static class DocumentFinder extends Finder<Long,Document> {
    DocumentFinder() {
      super(Document.class);
    }

    public Document asDraft(Long id) {
      return query().asDraft().setId(id).findOne();
    }
  }
}
