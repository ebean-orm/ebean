package io.ebeaninternal.server.type;

import org.junit.Test;

import java.sql.Date;

import static org.junit.Assert.assertEquals;

public class ScalarTypeDateTest {

  ScalarTypeDate type = new ScalarTypeDate();

  @Test
  public void formatParse_PG_DATE_POSITIVE_INFINITY() {

    Date postgresInfinityDate = new Date(9223372036825200000L);

    String format = type.formatValue(postgresInfinityDate);
    Date parsed = type.parse(format);
    assertEquals(parsed, postgresInfinityDate);
  }
}
