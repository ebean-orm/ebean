package org.tests.inheritance.company.domain;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import junit.framework.TestCase;
import org.junit.Assert;

import java.util.List;

public class TestInheritAbstract extends TestCase {

  public void testMe() {

    EbeanServer server = Ebean.getServer(null);

    List<AbstractBar> list0 = server.find(AbstractBar.class)
      .findList();

    Assert.assertNotNull(list0);

    List<AbstractBar> list1 = server.find(AbstractBar.class)
      .fetch("foo", "importantText")
      .findList();

    Assert.assertNotNull(list1);

    Foo f = new Foo();
    f.setImportantText("blah");

    server.save(f);

    ConcreteBar cb = new ConcreteBar();
    cb.setFoo(f);
    server.save(cb);

    List<AbstractBar> list2 = server.find(AbstractBar.class)
      .fetch("foo", "importantText")
      .findList();

    Assert.assertNotNull(list2);
    Assert.assertTrue(!list2.isEmpty());

    for (AbstractBar abstractBar : list2) {
      Foo foo = abstractBar.getFoo();
      String importantText = foo.getImportantText();
      Assert.assertNotNull(importantText);
    }


  }

}
