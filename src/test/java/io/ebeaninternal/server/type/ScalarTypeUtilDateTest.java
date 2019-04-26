package io.ebeaninternal.server.type;

import io.ebean.config.JsonConfig;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class ScalarTypeUtilDateTest {

  private ScalarTypeUtilDate.DateType dateType = new ScalarTypeUtilDate.DateType(JsonConfig.Date.MILLIS);

  @Test
  public void json() throws IOException {

    Date val = dateType.parse("2019-05-09");

    JsonTester<Date> jsonMillis = new JsonTester<>(dateType);
    assertThat(jsonMillis.test(val)).isEqualTo("{\"key\":1557316800000}");

    JsonTester<Date> jsonIso = new JsonTester<>(new ScalarTypeUtilDate.DateType(JsonConfig.Date.ISO8601) );
    assertThat(jsonIso.test(val)).isEqualTo("{\"key\":\"2019-05-09\"}");
  }
}
