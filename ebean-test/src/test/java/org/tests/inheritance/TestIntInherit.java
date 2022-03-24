package org.tests.inheritance;


import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.TIntChild;
import org.tests.model.basic.TIntRoot;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestIntInherit extends BaseTestCase {

  @Test
  public void testMe() {

    TIntRoot r = new TIntRoot();
    r.setName("root1");

    TIntRoot r2 = new TIntRoot();
    r.setName("root2");

    TIntChild c1 = new TIntChild();
    c1.setName("child1");
    c1.setChildProperty("cp1");

    TIntChild c2 = new TIntChild();
    c2.setName("child2");
    c2.setChildProperty("cp2");


    DB.save(r);
    DB.save(r2);
    DB.save(c1);
    DB.save(c2);

    TIntRoot result1 = DB.find(TIntRoot.class, r.getId());
    assertTrue(result1 instanceof TIntRoot);

    TIntRoot ref3 = DB.reference(TIntRoot.class, c1.getId());
    assertTrue(ref3 instanceof TIntRoot);

    TIntRoot result3 = DB.find(TIntRoot.class, c1.getId());
    assertTrue(result3 instanceof TIntChild);

  }

}
