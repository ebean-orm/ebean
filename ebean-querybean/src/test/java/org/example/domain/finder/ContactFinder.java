package org.example.domain.finder;

import io.ebean.Finder;
import org.example.domain.Contact;
import org.example.domain.query.QContact;

/**
 */
public class ContactFinder extends Finder<Long, Contact> {

  public ContactFinder() {
    super(Contact.class);
  }

  public QContact typed() {
    return new QContact();
  }
}
