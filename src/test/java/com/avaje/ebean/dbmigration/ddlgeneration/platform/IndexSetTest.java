package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import org.junit.Test;

import static org.junit.Assert.*;


public class IndexSetTest {


  @Test
  public void test() {

    BaseTableDdl.IndexSet set = new BaseTableDdl.IndexSet();
    assertTrue(set.add(new String[]{"one_column"}));
    assertTrue(set.add(new String[]{"two_column"}));
    assertFalse(set.add(new String[]{"one_column"}));

    assertTrue(set.add(new String[]{"a", "b"}));
    assertTrue(set.add(new String[]{"b","c"}));
    assertTrue(set.add(new String[]{"a"}));
    assertFalse(set.add(new String[]{"a", "b"}));

  }
}