package io.ebeaninternal.server.type;


import com.fasterxml.jackson.core.JsonParser;
import io.ebean.DB;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;

public class ScalarTypeIntegerTest {

  private final ScalarTypeInteger type = new ScalarTypeInteger();

  @Test
  public void format_when_string() {
    assertThat(type.format("1")).isEqualTo("1");
  }

  @Test
  public void format_when_integer() {
    assertThat(type.format(1)).isEqualTo("1");
  }

  @Test
  public void json() throws IOException {

    JsonParser parser = DB.json().createParser(new StringReader("1"));
    parser.nextToken();
    Object parsed = type.jsonRead(parser);
    assertThat(parsed).isEqualTo(1);

    parser = DB.json().createParser(new StringReader("\"1.0\""));
    parser.nextToken();
    parsed = type.jsonRead(parser);
    assertThat(parsed).isEqualTo(1);
  }

}
