package org.tests.inheritance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.ebean.test.LoggedSql;
import org.tests.inherit.ChildA;
import org.tests.inherit.ChildB;
import org.tests.inherit.Parent;
import org.junit.jupiter.api.Test;

import io.ebean.BaseTestCase;

public class TestInheritanceRefBean extends BaseTestCase {

    @Test
    public void test() {
      Parent a = new ChildA(42, "Bean A");
      Parent b = new ChildB(43, "Bean B");

      server().save(a);
      server().save(b);
      Long idA = a.getId();
      Long idB = b.getId();

      Parent test;

      LoggedSql.start();
      test = server().reference(ChildA.class, idA);
      assertTrue(test instanceof ChildA);
      assertEquals(0, LoggedSql.stop().size());

      LoggedSql.start();
      test = server().reference(Parent.class, idB);
      assertTrue(test instanceof ChildB);
      assertEquals(1, LoggedSql.stop().size());


    }
}
