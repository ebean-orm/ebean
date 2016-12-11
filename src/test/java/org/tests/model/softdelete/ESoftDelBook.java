package org.tests.model.softdelete;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.List;

@Entity
public class ESoftDelBook extends BaseSoftDelete {

  String bookTitle;

  @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
  List<ESoftDelUser> lendBys;

  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
  ESoftDelUser lendBy;


  public ESoftDelBook(String bookTitle) {
    this.bookTitle = bookTitle;
  }

  public ESoftDelBook() {
  }

  public String getBookTitle() {
    return bookTitle;
  }

  public void setBookTitle(String bookTitle) {
    this.bookTitle = bookTitle;
  }

  public List<ESoftDelUser> getLendBys() {
    return lendBys;
  }

  public void setLendBys(List<ESoftDelUser> lendBys) {
    this.lendBys = lendBys;
  }

  public void setLendBy(ESoftDelUser lendBy) {
    this.lendBy = lendBy;
  }

  public ESoftDelUser getLendBy() {
    return lendBy;
  }
}
