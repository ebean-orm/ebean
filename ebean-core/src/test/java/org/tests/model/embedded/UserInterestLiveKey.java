package org.tests.model.embedded;

import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class UserInterestLiveKey {

  private long userId;
  private long liveId;

  public UserInterestLiveKey(long userId, long liveId) {
    this.userId = userId;
    this.liveId = liveId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserInterestLiveKey that = (UserInterestLiveKey) o;
    return userId == that.userId &&
      liveId == that.liveId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, liveId);
  }
}
