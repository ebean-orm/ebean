package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.Country;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class TestLoadBeanCache extends BaseTestCase {

  @Test
  public void testLoad() {

    ResetBasicData.reset();

    Map<String, Country> map = Ebean.find(Country.class)
      .setLoadBeanCache(true)
      .setUseQueryCache(true)
      .setReadOnly(true)
      .order("name")
      .findMap();

    Country loadedNz = map.get("NZ");

    // this will hit the cache
    Country nz = Ebean.find(Country.class, "NZ");

    Assert.assertTrue(loadedNz == nz);
  }
}
