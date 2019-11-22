package io.ebeaninternal.server.query;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DbOrderByTrimTest {

  @Test
  public void trim() {

    String[] tests = {"foo", "bar", "a b c", "some_", "some()", "some'g'", "some('sd')", "some(a.b, '<s d>')"};
    for (String value : tests) {
      test(value, " desc");
    }
    for (String value : tests) {
      test(value, " asc");
    }
  }

  private void test(String value, String suffix) {
    assertEquals(value, DbOrderByTrim.trim(value + suffix));
  }

  @Test
  public void test_when_pgp_sym_decrypt() {
    assertEquals("pgp_sym_decrypt(t0.columnName, '<encryption key>')", DbOrderByTrim.trim("pgp_sym_decrypt(t0.columnName, '<encryption key>') desc"));
  }

  @Test
  public void trim_doubleSpace() {
    assertEquals("foo  bar", DbOrderByTrim.trim("foo  desc bar"));
  }

  @Test
  public void trim_description() {
    assertEquals("foo description", DbOrderByTrim.trim("foo description"));
    assertEquals("foo ription", DbOrderByTrim.trim("foo desc ription"));
  }

  @Test
  public void trim_asc1() {
    assertEquals("foo asc1", DbOrderByTrim.trim("foo asc1"));
    assertEquals("foo 1", DbOrderByTrim.trim("foo asc 1"));
  }

  @Test
  public void trim_various() {
    assertEquals("foo bar", DbOrderByTrim.trim("foo asc desc bar"));
    assertEquals("foo bar", DbOrderByTrim.trim("foo desc asc desc asc asc bar"));

    assertEquals("foo", DbOrderByTrim.trim("foo DESC"));
    assertEquals("foo ", DbOrderByTrim.trim("foo DESC "));
    assertEquals("foo", DbOrderByTrim.trim("foo ASC"));
    assertEquals("foo ", DbOrderByTrim.trim("foo ASC "));
  }

  @Test
  public void trim_nulls() {
    assertEquals("foo", DbOrderByTrim.trim("foo nulls first asc"));
    assertEquals("foo", DbOrderByTrim.trim("foo nulls last desc"));
    assertEquals("foo", DbOrderByTrim.trim("foo asc nulls first"));
    assertEquals("foo", DbOrderByTrim.trim("foo desc nulls last"));
  }

}
