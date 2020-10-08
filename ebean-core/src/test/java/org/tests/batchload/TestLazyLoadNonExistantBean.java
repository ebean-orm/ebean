package org.tests.batchload;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.UUOne;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.EntityNotFoundException;
import java.util.UUID;

public class TestLazyLoadNonExistantBean extends BaseTestCase {

  @Test
  public void testSimple() {

    UUID uuid = UUID.randomUUID();
    UUOne one = Ebean.getReference(UUOne.class, uuid);

    try {
      // invoke lazy loading
      one.getName();
      Assert.assertTrue(false);
    } catch (EntityNotFoundException e) {
      // expecting this
      Assert.assertTrue(true);
    }
  }
}
