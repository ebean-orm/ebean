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

  /**
   * @return the id
   */
  public Integer getId() {
    return this.id;
  }

  /**
   * @param id the id to set
   */
  public void setId(final Integer id) {
    this.id = id;
  }

  /**
   * @return the bookmarkReference
   */
  public String getBookmarkReference() {
    return this.bookmarkReference;
  }

  /**
   * @param bookmarkReference the bookmarkReference to set
   */
  public void setBookmarkReference(final String bookmarkReference) {
    this.bookmarkReference = bookmarkReference;
  }

  /**
   * @return the user
   */
  public BBookmarkUser getUser() {
    return this.user;
  }

  /**
   * @param user the user to set
   */
  public void setUser(final BBookmarkUser user) {
    this.user = user;
  }
}
