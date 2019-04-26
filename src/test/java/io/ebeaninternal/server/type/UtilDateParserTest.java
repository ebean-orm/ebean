package io.ebeaninternal.server.type;

import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class UtilDateParserTest {

  @Test
  public void parse() {

    Date val = UtilDateParser.parse("2019-05-09");
    String format = UtilDateParser.format(val);
    assertThat(format).isEqualTo("2019-05-09");
  }
}
