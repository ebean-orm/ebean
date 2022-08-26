package io.ebean.core.type;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class IsoJsonDateTimeParserTest {

  private ScalarTypeUtils parser = new ScalarTypeUtils();

  @Test
  void parseFormat_when_hasMillis() {
    parseAndFormat("2016-02-28T20:39:00.123Z", "2016-02-28T20:39:00.123Z");
  }

  @Test
  void parseFormat_when_noMillis() {
    parseAndFormat("2016-02-28T20:39:00Z", "2016-02-28T20:39:00.000Z");
  }

  @Test
  void parseFormat_when_millis_1dp() {
    parseAndFormat("2016-02-28T20:39:00.0Z", "2016-02-28T20:39:00.000Z");
  }

  @Test
  void parseFormat_when_millis_2dp() {
    parseAndFormat("2016-02-28T20:39:00.00Z", "2016-02-28T20:39:00.000Z");
  }

  @Test
  void parseFormat_when_millis_3dp() {
    parseAndFormat("2016-02-28T20:39:00.000Z", "2016-02-28T20:39:00.000Z");
  }

  @Test
  void parseFormat_when_millis_3dp_2() {
    parseAndFormat("2016-02-28T20:39:32.999000Z", "2016-02-28T20:39:32.999Z");
  }

  private void parseAndFormat(String input, String expected) {
    Instant timestamp = parser.parseInstant(input);
    String format = parser.formatInstant(timestamp);
    assertThat(format).isEqualTo(expected);
  }
}
