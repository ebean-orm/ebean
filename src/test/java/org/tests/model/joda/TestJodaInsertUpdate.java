package org.tests.model.joda;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class TestJodaInsertUpdate extends BaseTestCase {

  @Test
  public void test() throws InterruptedException {

    BasicJodaEntity e0 = new BasicJodaEntity();
    e0.setName("foo");

    Ebean.save(e0);

    LocalDateTime created = e0.getCreated();
    DateTime updated = e0.getUpdated();
    LocalDateTime version = e0.getVersion();
    assertNotNull(created);
    assertNotNull(updated);
    assertNotNull(version);

    Thread.sleep(10);
    e0.setName("bar");
    e0.setPeriod(Period.years(12).plusDays(1));
    Ebean.save(e0);

    LocalDateTime created1 = e0.getCreated();
    DateTime updated1 = e0.getUpdated();
    LocalDateTime version1 = e0.getVersion();

    assertSame(created, created1);
    assertNotSame(updated, updated1);
    assertNotSame(version, version1);


    BasicJodaEntity found = Ebean.find(BasicJodaEntity.class, e0.getId());

    assertThat(found.getPeriod()).isEqualTo(e0.getPeriod());
  }
}
