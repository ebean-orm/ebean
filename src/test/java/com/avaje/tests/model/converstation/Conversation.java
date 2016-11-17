package com.avaje.tests.model.converstation;

import com.avaje.tests.model.BaseModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "c_conversation")
public class Conversation extends BaseModel {

  String title;

  @Column(name = "isopen")
  boolean open;

  @ManyToOne
  Group group;

  @OneToMany(mappedBy = "conversation")
  List<Participation> participants;

  @OneToMany(mappedBy = "conversation")
  List<Message> messages;


  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public boolean isOpen() {
    return open;
  }

  public void setOpen(boolean open) {
    this.open = open;
  }

  public Group getGroup() {
    return group;
  }

  public void setGroup(Group group) {
    this.group = group;
  }

  public List<Message> getMessages() {
    return messages;
  }

  public void setMessages(List<Message> messages) {
    this.messages = messages;
  }

}
