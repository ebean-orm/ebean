package org.tests.inheritance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.avaje.ebeantest.LoggedSqlCollector;
import org.avaje.test.model.rawsql.inherit.ChildA;
import org.avaje.test.model.rawsql.inherit.ChildB;
import org.avaje.test.model.rawsql.inherit.Parent;
import org.junit.Test;

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
      
      LoggedSqlCollector.start();
      test = server().getReference(ChildA.class, idA);
      assertTrue(test instanceof ChildA);
      assertEquals(0, LoggedSqlCollector.stop().size());
      
      LoggedSqlCollector.start();
      test = server().getReference(Parent.class, idB);
      assertTrue(test instanceof ChildB);
      assertEquals(1, LoggedSqlCollector.stop().size());
      
      
    }
}
