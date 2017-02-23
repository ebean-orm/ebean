package org.tests.idkeys;

import org.junit.Test;
import org.tests.model.basic.TOne;

import io.ebean.BaseTestCase;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;

/**
 * Test lazy loading
 */
public class TestLazyLoad extends BaseTestCase {
  /**
   * This test loads just a single property of the Entity AuditLog and later on access
   * the description which should force a lazy load of this property
   */
  @Test
  public void testPartialLoad() throws SQLException {
    TOne log = new TOne();
    log.setName("test partial");
    log.setDescription("log");

    server().save(log);

    assertNotNull(log.getId());

    List<TOne> logs = server().find(TOne.class)
      .select("id")
      .where().eq("id", log.getId())
      .findList();

    assertNotNull(logs);
    assertEquals(1, logs.size());

    TOne logLazy = logs.get(0);

    String description = logLazy.getDescription();
    assertEquals(log.getDescription(), description);
  }
}
