package com.avaje.tests.batchload;

import java.util.UUID;

import javax.persistence.EntityNotFoundException;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.UUOne;

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
