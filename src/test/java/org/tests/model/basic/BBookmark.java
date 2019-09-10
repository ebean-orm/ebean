package org.tests.model.basic;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * A user may have multiple bookmarks
 *
 * @author Chris
 */
@Entity
public class BBookmark {

  @Id
  @GeneratedValue
  private Integer id;

  @Column
  private String bookmarkReference;

  @ManyToOne(cascade = CascadeType.PERSIST)
  @Column
  private BBookmarkUser user;

  public Integer getId() {
    return this.id;
  }

  public void setId(final Integer id) {
    this.id = id;
  }

  public String getBookmarkReference() {
    return this.bookmarkReference;
  }

  public void setBookmarkReference(final String bookmarkReference) {
    this.bookmarkReference = bookmarkReference;
  }

  public BBookmarkUser getUser() {
    return this.user;
  }

  public void setUser(final BBookmarkUser user) {
    this.user = user;
  }
}
