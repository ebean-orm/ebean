package org.tests.model.embedded;

import io.ebean.Model;
import io.ebean.annotation.CreatedTimestamp;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.util.Date;

@Entity
public class UserInterestLive extends Model {

  @EmbeddedId
  private final UserInterestLiveKey key;

  @CreatedTimestamp
  private Date createdAt;

  public UserInterestLive(UserInterestLiveKey key) {
    this.key = key;
  }

  public UserInterestLiveKey getKey() {
    return key;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }
}
