package org.tests.text.csv;

import io.ebean.BaseTestCase;
import io.ebean.text.TimeStringParser;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Time;

public class TestTimeStringParser extends BaseTestCase {

  @Test
  public void testSimple() {

    Time t = (Time) TimeStringParser.get().parse("12:00");
    Assert.assertNotNull(t);

    t = (Time) TimeStringParser.get().parse("12:00:12");
    Assert.assertNotNull(t);

    expectError("12");
    expectError("12:");
    expectError("12:00:");
    expectError("12:00::");
    expectError("12:00:00:");

  }

  private void expectError(String value) {
    try {
      TimeStringParser.get().parse(value);
      Assert.assertTrue(false);
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(true);
    }
  }
}
