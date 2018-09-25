package org.tests.merge;

import io.ebean.annotation.NotNull;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class MContactMessage extends MBase {

  private String title;

  private String subject;

  private String notes;

  @NotNull
  @ManyToOne
  private MContact contact;

  public MContactMessage(String title, String subject) {
    this.title = title;
    this.subject = subject;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public MContact getContact() {
    return contact;
  }

  public void setContact(MContact contact) {
    this.contact = contact;
  }
}
