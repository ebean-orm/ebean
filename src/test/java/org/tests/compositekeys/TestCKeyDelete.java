package org.tests.compositekeys;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.CKeyParent;
import org.tests.model.basic.CKeyParentId;
import org.junit.Assert;
import org.junit.Test;

public class TestCKeyDelete extends BaseTestCase {

  @Test
  public void test() {

    CKeyParentId id = new CKeyParentId(100, "deleteMe");
    CKeyParentId searchId = new CKeyParentId(100, "deleteMe");

    CKeyParent p = new CKeyParent();
    p.setId(id);
    p.setName("testDelete");

    Ebean.save(p);

    CKeyParent found = Ebean.find(CKeyParent.class).where().idEq(searchId).findOne();

    Assert.assertNotNull(found);

    Ebean.delete(CKeyParent.class, searchId);

    CKeyParent notFound = Ebean.find(CKeyParent.class).where().idEq(searchId).findOne();

    Assert.assertNull(notFound);

  }
}
