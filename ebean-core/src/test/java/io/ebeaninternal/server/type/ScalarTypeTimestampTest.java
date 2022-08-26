package io.ebeaninternal.server.type;

import io.ebean.config.JsonConfig;
import io.ebean.core.type.IsoJsonDateTimeParser;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

public class ScalarTypeTimestampTest {

  @Test
  public void toJsonISO8601() {

    ScalarTypeTimestamp typeIso = new ScalarTypeTimestamp(JsonConfig.DateTime.ISO8601);

    Timestamp now = new Timestamp(System.currentTimeMillis());
    String asJson = typeIso.toJsonISO8601(now);

    Timestamp value = typeIso.convertFromInstant(IsoJsonDateTimeParser.parseIso(asJson)); //typeIso.fromJsonISO8601(asJson);
    assertThat(now).isEqualTo(value);
  }
}
