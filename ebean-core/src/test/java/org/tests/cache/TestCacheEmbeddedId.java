package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.Test;
import org.tests.model.basic.CKeyParent;
import org.tests.model.basic.CKeyParentId;

import static org.junit.Assert.assertNotNull;

public class TestCacheEmbeddedId extends BaseTestCase {

  @Test
  public void test() {

    CKeyParentId id = new CKeyParentId(100, "testEmbedded");

    CKeyParent p = new CKeyParent();
    p.setId(id);
    p.setName("testCache");

    DB.save(p);

    CKeyParent cKeyParent = DB.find(CKeyParent.class, id);
    assertNotNull(cKeyParent);

    // find for second time
    // causes java.lang.ClassCastException: class java.lang.String cannot be cast to class org.tests.model.basic.CKeyParentId
    cKeyParent = DB.find(CKeyParent.class, id);
    assertNotNull(cKeyParent);
  }
}
