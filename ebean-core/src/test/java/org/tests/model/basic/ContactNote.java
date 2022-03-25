package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Size;

@Entity
public class ContactNote extends BasicDomain {

  private static final long serialVersionUID = 7949702621226333278L;

  @ManyToOne
  Contact contact;

  String title;

  @Size(max = 2000)
  @Lob
  String note;

  public ContactNote(String title, String note) {
    this.title = title;
    this.note = note;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public Contact getContact() {
    return contact;
  }

  public void setContact(Contact contact) {
    this.contact = contact;
  }

}
