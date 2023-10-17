package org.example.domain.other;

import jakarta.persistence.*;

import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("unused")
@Entity
public class OtherSiteUser {

  @Embeddable
  public static class Id {

    // Use siteId, userId matching by db column naming convention
    public UUID siteId;
    public UUID userId;

    public Id(UUID siteId, UUID userId) {
      this.siteId = siteId;
      this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Id that = (Id) o;
      return Objects.equals(siteId, that.siteId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(siteId, userId);
    }
  }

  @EmbeddedId
  Id id;

  String description;

  @Version
  long version;

 }
