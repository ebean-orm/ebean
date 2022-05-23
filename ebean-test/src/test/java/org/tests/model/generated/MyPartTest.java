package org.tests.model.generated;

import io.ebean.DB;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class MyPartTest {

  @Test
  void insert_find() {
    Instant now = Instant.now();
    MyPart part = new MyPart(now);
    part.setMetaInfo("meta");

    DB.save(part);
    assertThat(part.getId()).isGreaterThan(0);

    MyPart found = DB.find(MyPart.class, part.getId());
    assertThat(found).isNotNull();
  }
}
