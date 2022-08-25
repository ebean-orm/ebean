package io.ebean.csv.reader;

import org.junit.jupiter.api.Test;

import java.sql.Time;

import static org.junit.jupiter.api.Assertions.*;

public class TimeStringParserTest {

  @Test
  public void testSimple() {

    Time t = (Time) TimeStringParser.get().parse("12:00");
    assertNotNull(t);

    t = (Time) TimeStringParser.get().parse("12:00:12");
    assertNotNull(t);

    expectError("12");
    expectError("12:");
    expectError("12:00:");
    expectError("12:00::");
    expectError("12:00:00:");

  }

  private void expectError(String value) {
    try {
      TimeStringParser.get().parse(value);
      fail();
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }
  }
}
