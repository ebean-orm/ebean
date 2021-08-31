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
    userSite.setNote("HelloIdClass");
    userSite.save();

    var id = new UserSiteId(userId, siteId);

    UserSite found = new QUserSite()
      .setId(id)
      .findOne();

    assertThat(found.getUserId()).isEqualTo(userId);
    assertThat(found.getSiteId()).isEqualTo(siteId);
    assertThat(found.getNote()).isEqualTo("HelloIdClass");


  }
}
