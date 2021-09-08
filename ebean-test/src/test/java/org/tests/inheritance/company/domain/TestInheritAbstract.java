package org.tests.inheritance.company.domain;

import io.ebean.DB;
import io.ebean.Database;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestInheritAbstract {

  @Test
  public void testMe() {

    Database server = DB.getDefault();

    List<AbstractBar> list0 = server.find(AbstractBar.class)
      .findList();

    assertNotNull(list0);

    List<AbstractBar> list1 = server.find(AbstractBar.class)
      .fetch("foo", "importantText")
      .findList();

    assertNotNull(list1);

    Foo f = new Foo();
    f.setImportantText("blah");

    server.save(f);

    ConcreteBar cb = new ConcreteBar();
    cb.setFoo(f);
    server.save(cb);

    List<AbstractBar> list2 = server.find(AbstractBar.class)
      .fetch("foo", "importantText")
      .findList();

    assertNotNull(list2);
    assertThat(list2).isNotEmpty();

    for (AbstractBar abstractBar : list2) {
      Foo foo = abstractBar.getFoo();
      String importantText = foo.getImportantText();
      assertNotNull(importantText);
    }


  }

}
