package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Country;
import org.tests.model.basic.ResetBasicData;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestLoadBeanCache extends BaseTestCase {

  @Test
  public void testLoad() {

    ResetBasicData.reset();

    Map<String, Country> map = DB.find(Country.class)
      .setLoadBeanCache(true)
      .setUseQueryCache(true)
      .setReadOnly(true)
      .order("name")
      .findMap();

    Country loadedNz = map.get("NZ");

    // this will hit the cache
    Country nz = DB.find(Country.class, "NZ");

    assertTrue(loadedNz == nz);
  }
}
