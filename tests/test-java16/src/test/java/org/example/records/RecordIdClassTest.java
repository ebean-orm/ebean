package org.example.records;

import org.example.records.query.QUserSite;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RecordIdClassTest {

  @Test
  void insert_query() {

    UUID userId = UUID.randomUUID();
    UUID siteId = UUID.randomUUID();

    var userSite = new UserSite(userId, siteId);
    userSite.note("HelloIdClass");
    userSite.save();

    var id = new UserSiteId(userId, siteId);

    UserSite found = new QUserSite()
      .setId(id)
      .findOne();

    assert found != null;
    assertThat(found.userId()).isEqualTo(userId);
    assertThat(found.siteId()).isEqualTo(siteId);
    assertThat(found.note()).isEqualTo("HelloIdClass");

    // again but using the scalar id properties
    UserSite found2 = new QUserSite()
      .siteId.eq(id.siteId())
      .userId.eq(id.userId())
      .findOne();

    assert found2 != null;
    assertThat(found2.userId()).isEqualTo(userId);
    assertThat(found2.siteId()).isEqualTo(siteId);
    assertThat(found2.note()).isEqualTo("HelloIdClass");
  }
}
