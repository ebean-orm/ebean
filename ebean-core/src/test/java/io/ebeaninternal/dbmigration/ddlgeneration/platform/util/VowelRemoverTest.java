package io.ebeaninternal.dbmigration.ddlgeneration.platform.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VowelRemoverTest {

  @Test
  public void testTrim() throws Exception {


    assertEquals("fk_abcd", VowelRemover.trim("fk_abcde", 4));
    assertEquals("fk_a", VowelRemover.trim("fk_aaaaaa", 4));
    assertEquals("ab_avrylngtblnm", VowelRemover.trim("ab_averylongtablename", 4));
  }
}
