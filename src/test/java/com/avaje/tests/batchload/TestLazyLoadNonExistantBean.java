package com.avaje.tests.batchload;

import java.util.UUID;

import javax.persistence.EntityNotFoundException;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.UUOne;

public class TestLazyLoadNonExistantBean extends TestCase {

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
