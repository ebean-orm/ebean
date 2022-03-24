package org.tests.batchload;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.UUOne;

import javax.persistence.EntityNotFoundException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestLazyLoadNonExistentBean extends BaseTestCase {

  @Test
  public void testSimple() {

    UUID uuid = UUID.randomUUID();
    UUOne one = DB.reference(UUOne.class, uuid);

    try {
      // invoke lazy loading
      one.getName();
      assertTrue(false);
    } catch (EntityNotFoundException e) {
      // expecting this
      assertTrue(true);
    }
  }
}
