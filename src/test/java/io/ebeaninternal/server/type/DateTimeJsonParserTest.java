package io.ebeaninternal.server.type;


import org.junit.Test;

import java.sql.Timestamp;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class DateTimeJsonParserTest {

  @Test
  public void parseFormat_when_hasMillis() throws Exception {

    DateTimeJsonParser parser = new DateTimeJsonParser();

    String input = "2016-02-28T20:39:00.123Z";
    String formatted = parseAndFormat(parser, input);

    assertThat(formatted).isEqualTo("2016-02-28T20:39:00.123Z");
  }

  @Test
  public void parseFormat_when_noMillis() throws Exception {

    DateTimeJsonParser parser = new DateTimeJsonParser();

    String input = "2016-02-28T20:39:00Z";
    String formatted = parseAndFormat(parser, input);

    assertThat(formatted).isEqualTo("2016-02-28T20:39:00.000Z");
  }

  @Test
  public void parseFormat_when_millis_1dp() throws Exception {

    DateTimeJsonParser parser = new DateTimeJsonParser();

    String input = "2016-02-28T20:39:00.0Z";
    String formatted = parseAndFormat(parser, input);

    assertThat(formatted).isEqualTo("2016-02-28T20:39:00.000Z");
  }

  @Test
  public void parseFormat_when_millis_2dp() throws Exception {

    DateTimeJsonParser parser = new DateTimeJsonParser();

    String input = "2016-02-28T20:39:00.00Z";
    String formatted = parseAndFormat(parser, input);

    assertThat(formatted).isEqualTo("2016-02-28T20:39:00.000Z");
  }

  @Test
  public void parseFormat_when_millis_3dp() throws Exception {

    DateTimeJsonParser parser = new DateTimeJsonParser();

    String input = "2016-02-28T20:39:00.000Z";
    String formatted = parseAndFormat(parser, input);

    assertThat(formatted).isEqualTo("2016-02-28T20:39:00.000Z");
  }

  private String parseAndFormat(DateTimeJsonParser parser, String input) {
    Timestamp timestamp = parser.parse(input);
    return parser.format(timestamp);
  }
}
