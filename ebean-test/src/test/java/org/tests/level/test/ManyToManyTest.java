package org.tests.level.test;

import io.ebean.Transaction;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.level.Level1;
import org.tests.level.Level2;
import org.tests.level.Level3;
import org.tests.level.Level4;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ManyToManyTest extends BaseTestCase {

  @Test
  public void test() {

    try (Transaction txn = DB.beginTransaction()) {
      Level4 i = new Level4("i");
      Level4 ii = new Level4("ii");
      Level4 iii = new Level4("iii");

      DB.save(i);
      DB.save(ii);
      DB.save(iii);

      Level3 a = new Level3("a");
      Level3 b = new Level3("b");

      DB.save(a);
      DB.save(b);

      Level2 one = new Level2("one");
      Level2 two = new Level2("two");

      DB.save(one);
      DB.save(two);

      Level1 x1 = new Level1("x1");
      Level1 x2 = new Level1("x2");
      Level1 x3 = new Level1("x3");
      Level1 x4 = new Level1("x4");
      Level1 x5 = new Level1("x5");

      x1.getLevel2s().add(one);
      x2.getLevel2s().add(one);
      x3.getLevel2s().add(two);
      x4.getLevel2s().add(two);
      x5.getLevel2s().add(two);

      x1.getLevel4s().add(i);
      x1.getLevel4s().add(ii);
      x2.getLevel4s().add(ii);
      x2.getLevel4s().add(iii);

      DB.save(x1);
      DB.save(x2);
      DB.save(x3);
      DB.save(x4);
      DB.save(x5);

      // this query had the original problem
      List<Level1> things = DB.find(Level1.class)
        .fetch("level4s")
        .fetch("level2s")
        .fetch("level2s.level3s")
        .orderBy().asc("id")
        .findList();

      validateObjectGraph(i, ii, iii, one, two, x1, x2, x3, x4, x5, things);


      things = DB.find(Level1.class)
        .fetch("level2s")
        .fetch("level2s.level3s")
        .fetch("level4s")
        .orderBy().asc("id")
        .findList();

      validateObjectGraph(i, ii, iii, one, two, x1, x2, x3, x4, x5, things);


      things = DB.find(Level1.class)
        .fetch("level2s")
        .fetch("level4s")
        .orderBy().asc("id")
        .findList();

      validateObjectGraph(i, ii, iii, one, two, x1, x2, x3, x4, x5, things);
    }
  }

  private void validateObjectGraph(Level4 i, Level4 ii, Level4 iii, Level2 one, Level2 two, Level1 x1, Level1 x2, Level1 x3, Level1 x4, Level1 x5, List<Level1> things) {

    assertEquals(5, things.size());

    // x1's are in order expected
    assertEquals(x1.getId(), things.get(0).getId());
    assertEquals(x2.getId(), things.get(1).getId());
    assertEquals(x3.getId(), things.get(2).getId());
    assertEquals(x4.getId(), things.get(3).getId());
    assertEquals(x5.getId(), things.get(4).getId());

    // x1 to level4s
    assertEquals(2, things.get(0).getLevel4s().size());
    assertTrue(contains(things.get(0).getLevel4s(), i));
    assertTrue(contains(things.get(0).getLevel4s(), ii));

    // x2 to level4s
    assertEquals(2, things.get(1).getLevel4s().size());
    assertTrue(contains(things.get(1).getLevel4s(), ii));
    assertTrue(contains(things.get(1).getLevel4s(), iii));

    // x3 etc to level4s
    assertEquals(0, things.get(2).getLevel4s().size());
    assertEquals(0, things.get(3).getLevel4s().size());
    assertEquals(0, things.get(4).getLevel4s().size());

    // level2 relationships
    for (final Level1 curThing : things) {
      assertEquals(1, curThing.getLevel2s().size());
    }
    assertTrue(contains(things.get(0).getLevel2s(), one));
    assertTrue(contains(things.get(1).getLevel2s(), one));
    assertTrue(contains(things.get(2).getLevel2s(), two));
    assertTrue(contains(things.get(3).getLevel2s(), two));
    assertTrue(contains(things.get(4).getLevel2s(), two));
  }

  /**
   * Return true if match exists in level4s.
   */
  private boolean contains(Set<Level4> level4s, Level4 match) {
    for (Level4 level4 : level4s) {
      if (level4.getId().equals(match.getId()) && level4.getName().equals(match.getName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Return true if match exists in level2s.
   */
  private boolean contains(Set<Level2> level2s, Level2 match) {
    for (Level2 level2 : level2s) {
      if (level2.getId().equals(match.getId()) && level2.getName().equals(match.getName())) {
        return true;
      }
    }
    return false;
  }

}
