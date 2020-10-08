package org.tests.model.m2m;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Version;
import java.util.List;

@Entity
public class MailUser {

  @Id
  Long id;

  @Version
  Long version;

  String name;

  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(name = "mail_user_inbox")
  List<MailBox> inbox;

  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(name = "mail_user_outbox")
  List<MailBox> outbox;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<MailBox> getInbox() {
    return inbox;
  }

  public void setInbox(List<MailBox> inbox) {
    this.inbox = inbox;
  }

  public List<MailBox> getOutbox() {
    return outbox;
  }

  public void setOutbox(List<MailBox> outbox) {
    this.outbox = outbox;
  }
}

