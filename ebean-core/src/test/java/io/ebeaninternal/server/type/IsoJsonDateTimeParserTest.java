package io.ebeaninternal.server.type;


import org.junit.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class IsoJsonDateTimeParserTest {

  private IsoJsonDateTimeParser parser = new IsoJsonDateTimeParser();

  @Test
  public void parseFormat_when_hasMillis() {
    parseAndFormat("2016-02-28T20:39:00.123Z", "2016-02-28T20:39:00.123Z");
  }

  @Test
  public void parseFormat_when_noMillis() {
    parseAndFormat("2016-02-28T20:39:00Z", "2016-02-28T20:39:00.000Z");
  }

  @Test
  public void parseFormat_when_millis_1dp() {
    parseAndFormat("2016-02-28T20:39:00.0Z", "2016-02-28T20:39:00.000Z");
  }

  @Test
  public void parseFormat_when_millis_2dp() {
    parseAndFormat("2016-02-28T20:39:00.00Z", "2016-02-28T20:39:00.000Z");
  }

  @Test
  public void parseFormat_when_millis_3dp() {
    parseAndFormat("2016-02-28T20:39:00.000Z", "2016-02-28T20:39:00.000Z");
  }

  @Test
  public void parseFormat_when_millis_3dp_2() {
    parseAndFormat("2016-02-28T20:39:32.999000Z", "2016-02-28T20:39:32.999Z");
  }

  private void parseAndFormat(String input, String expected) {
    Instant timestamp = parser.parseIso(input);
    String format = parser.formatIso(timestamp);
    assertThat(format).isEqualTo(expected);
  }
}
