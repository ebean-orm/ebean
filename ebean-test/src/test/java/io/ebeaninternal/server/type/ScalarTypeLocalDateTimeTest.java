package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import io.ebean.config.JsonConfig;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class ScalarTypeLocalDateTimeTest {

  private final ScalarTypeLocalDateTime type = new ScalarTypeLocalDateTime(JsonConfig.DateTime.MILLIS);

  private final JsonFactory factory = new JsonFactory();

  // warm up
  private final LocalDateTime warmUp = LocalDateTime.now();

  @Test
  public void testNowToMillis() {

    warmUp.hashCode();

    long now = System.currentTimeMillis();
    long toMillis = type.convertToMillis(LocalDateTime.now());
    assertTrue(toMillis - now < 30);
  }

  @Test
  public void testConvertToMillis() {

    LocalDateTime now = LocalDateTime.now().withNano(123_000_000); // jdk11 workaround
    long asMillis = type.convertToMillis(now);
    LocalDateTime fromMillis = type.convertFromMillis(asMillis);

    assertEquals(now, fromMillis);
  }

  @Test
  public void testConvertFromTimestamp() {

    Timestamp now = new Timestamp(System.currentTimeMillis());

    LocalDateTime localDateTime = type.convertFromTimestamp(now);
    Timestamp timestamp = type.convertToTimestamp(localDateTime);

    assertEquals(now, timestamp);
  }


  @Test
  public void testToJdbcType() throws Exception {

    Object jdbcType = type.toJdbcType(LocalDateTime.now());
    assertTrue(jdbcType instanceof Timestamp);

    jdbcType = type.toJdbcType(new Timestamp(System.currentTimeMillis()));
    assertTrue(jdbcType instanceof Timestamp);
  }

  @Test
  public void testToBeanType() throws Exception {

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    LocalDateTime val1 = type.toBeanType(timestamp);
    assertNotNull(val1);

    LocalDateTime val2 = type.toBeanType(timestamp.toLocalDateTime());
    assertNotNull(val2);

    Timestamp timestamp1 = type.convertToTimestamp(val1);
    assertEquals(timestamp, timestamp1);
  }


  @Test
  public void testJsonRaw() throws Exception {

    final LocalDateTime of = LocalDateTime.of(2020, 5, 4, 13, 20, 40);

    ScalarTypeLocalDateTime typeIso = new ScalarTypeLocalDateTime(JsonConfig.DateTime.ISO8601);

    StringWriter writer = new StringWriter();
    JsonGenerator generator = factory.createGenerator(writer);

    typeIso.jsonWrite(generator, of);
    generator.flush();

    assertThat(of.toString()).isEqualTo("2020-05-04T13:20:40");
    assertThat(writer.toString()).isEqualTo("\"2020-05-04T13:20:40\"");
  }

  @Test
  public void testJson() throws Exception {

    LocalDateTime now = LocalDateTime.now().withNano(123_000_000); // jdk11 workaround

    JsonTester<LocalDateTime> jsonTester = new JsonTester<>(type);
    jsonTester.test(now);

    ScalarTypeLocalDateTime typeNanos = new ScalarTypeLocalDateTime(JsonConfig.DateTime.NANOS);
    jsonTester = new JsonTester<>(typeNanos);
    jsonTester.test(now);

    ScalarTypeLocalDateTime typeIso = new ScalarTypeLocalDateTime(JsonConfig.DateTime.ISO8601);
    jsonTester = new JsonTester<>(typeIso);
    jsonTester.test(now);
  }

  @Test
  public void isoJsonFormatParse() {

    ScalarTypeLocalDateTime typeIso = new ScalarTypeLocalDateTime(JsonConfig.DateTime.ISO8601);

    LocalDateTime localDateTime = LocalDateTime.now();
    String asJson = typeIso.toJsonISO8601(localDateTime);

    LocalDateTime value = typeIso.fromJsonISO8601(asJson);
    assertThat(localDateTime).isEqualToIgnoringNanos(value);
  }
  
  @Test
  public void testParseEbean11() {

    ScalarTypeLocalDateTime typeDefault = new ScalarTypeLocalDateTime(JsonConfig.DateTime.ISO8601);

    LocalDateTime fromMillis = typeDefault.parse("1517627106000");
    LocalDateTime fromIso= typeDefault.parse("2018-02-03T04:05:06");
    
    assertThat(fromMillis).isEqualToIgnoringNanos(fromIso);
  }
}
