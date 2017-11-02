package org.tests.inheritance;


import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.TIntChild;
import org.tests.model.basic.TIntRoot;
import org.junit.Assert;
import org.junit.Test;

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


    Ebean.save(r);
    Ebean.save(r2);
    Ebean.save(c1);
    Ebean.save(c2);

    TIntRoot result1 = Ebean.find(TIntRoot.class, r.getId());
    Assert.assertTrue(result1 instanceof TIntRoot);

    TIntRoot ref3 = Ebean.getReference(TIntRoot.class, c1.getId());
    Assert.assertTrue(ref3 instanceof TIntRoot);

    TIntRoot result3 = Ebean.find(TIntRoot.class, c1.getId());
    Assert.assertTrue(result3 instanceof TIntChild);

  }

}
