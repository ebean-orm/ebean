package io.ebeaninternal.server.type;

import io.ebean.config.JsonConfig;
import org.junit.Test;

import java.io.IOException;
import java.sql.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class ScalarTypeDateTest {

  private ScalarTypeDate type = new ScalarTypeDate(JsonConfig.Date.MILLIS);

  @Test
  public void formatParse_PG_DATE_POSITIVE_INFINITY() {

    Date postgresInfinityDate = new Date(9223372036825200000L);

    String format = type.formatValue(postgresInfinityDate);
    Date parsed = type.parse(format);
    assertEquals(parsed, postgresInfinityDate);
  }

  @Test
  public void json() throws IOException {


    Date val = new Date(1557360000000L);

    JsonTester<Date> jsonMillis = new JsonTester<>(type);
    assertThat(jsonMillis.test(val)).isEqualTo("{\"key\":1557360000000}");

    JsonTester<Date> jsonIso = new JsonTester<>(new ScalarTypeDate(JsonConfig.Date.ISO8601));
    Date val1 = jsonIso.type.parse("2019-05-09");
    assertThat(jsonIso.test(val1)).isEqualTo("{\"key\":\"2019-05-09\"}");
  }
}
