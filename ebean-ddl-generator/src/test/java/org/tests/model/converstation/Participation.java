package org.tests.model.converstation;

import org.tests.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "c_participation")
public class Participation extends BaseModel {

  public enum Type {
    Moderator,
    Member
  }

  Integer rating;

  Type type;

  @ManyToOne(optional = false)
  Conversation conversation;

  @ManyToOne(optional = false)
  User user;

  public Conversation getConversation() {
    return conversation;
  }

  public void setConversation(Conversation conversation) {
    this.conversation = conversation;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public Integer getRating() {
    return rating;
  }

  public void setRating(Integer rating) {
    this.rating = rating;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

}
