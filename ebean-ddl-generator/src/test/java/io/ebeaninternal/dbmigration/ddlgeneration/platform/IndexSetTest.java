package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebeaninternal.dbmigration.ddlgeneration.platform.util.IndexSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class IndexSetTest {


  @Test
  public void test() {

    IndexSet set = new IndexSet();
    assertTrue(set.add(new String[]{"one_column"}));
    assertTrue(set.add(new String[]{"two_column"}));
    assertFalse(set.add(new String[]{"one_column"}));

    assertTrue(set.add(new String[]{"a", "b"}));
    assertTrue(set.add(new String[]{"b", "c"}));
    assertTrue(set.add(new String[]{"a"}));
    assertFalse(set.add(new String[]{"a", "b"}));

  }
}
