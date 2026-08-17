package org.tests.model.deleteorder;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/**
 * Written from the delete callback of {@link DcoLink}, the way an audit or an outbox row is.
 */
@Entity
public class DcoAudit {

  @Id
  @GeneratedValue
  Long id;

  String message;

  public DcoAudit(String message) {
    this.message = message;
  }

  public Long getId() {
    return id;
  }

  public String getMessage() {
    return message;
  }
}
