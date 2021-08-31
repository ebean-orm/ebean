package io.ebeaninternal.server.type;


import com.fasterxml.jackson.core.JsonParser;
import io.ebean.DB;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.ScalarType;
import io.ebean.text.json.EJson;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class ScalarTypeArrayListTest extends BasePlatformArrayTypeFactoryTest {

  private final PlatformArrayTypeFactory factory = ScalarTypeArrayList.factory();

  @Test
  public void notSameInstance() {
    assertThat(factory.typeFor(UUID.class, true))
      .isNotSameAs(factory.typeFor(UUID.class, false));
  }

  @Test
  public void sameInstance_when_notNull() {
    assertThat(factory.typeFor(UUID.class, false))
      .isSameAs(factory.typeFor(UUID.class, false));
  }

  @Test
  public void sameInstance_when_nullable() {
    assertThat(factory.typeFor(UUID.class, true))
      .isSameAs(factory.typeFor(UUID.class, true));
  }

  @Test
  public void read_when_null() throws SQLException {

    DataReader mock = Mockito.mock(DataReader.class);
    Mockito.when(mock.getArray()).thenReturn(null);

    ScalarType<?> scalarType = ScalarTypeArrayList.factory().typeFor(Long.class, true);
    scalarType.read(mock);
  }

  @Test
  public void bindNullToEmpty_when_nullableIsFalse() throws SQLException {

    assertBindNullTo_EmptyArray(factory.typeFor(Integer.class, false));
    assertBindNullTo_EmptyArray(factory.typeFor(Long.class, false));
    assertBindNullTo_EmptyArray(factory.typeFor(Double.class, false));
    assertBindNullTo_EmptyArray(factory.typeFor(String.class, false));
    assertBindNullTo_EmptyArray(factory.typeFor(UUID.class, false));
  }

  @Test
  public void bindNullToNull_when_nullable() throws SQLException {

    assertBindNullTo_Null(factory.typeFor(Integer.class, true));
    assertBindNullTo_Null(factory.typeFor(Long.class, true));
    assertBindNullTo_Null(factory.typeFor(Double.class, true));
    assertBindNullTo_Null(factory.typeFor(String.class, true));
    assertBindNullTo_Null(factory.typeFor(UUID.class, true));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void parse_withUuidType_expect_uuidTypeReturned() {

    ScalarType<?> scalarType = factory.typeFor(UUID.class, true);

    List<UUID> input = new ArrayList<>();
    input.add(UUID.randomUUID());
    input.add(UUID.randomUUID());

    String formatToJson = scalarType.format(input);

    Object parsed = scalarType.parse(formatToJson);
    assertThat(parsed).isEqualTo(input);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void parse_withIntegerType_expect_longIntegerReturned() {

    ScalarType<?> scalarType = factory.typeFor(Integer.class, true);

    List<Integer> input = new ArrayList<>();
    input.add(2);
    input.add(4);

    String formatToJson = scalarType.format(input);

    Object parsed = scalarType.parse(formatToJson);
    assertThat(parsed).isEqualTo(input);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void parse_withLongType_expect_longTypeReturned() {

    ScalarType<?> scalarType = factory.typeFor(Long.class, true);

    List<Long> input = new ArrayList<>();
    input.add(2L);
    input.add(4L);

    String formatToJson = scalarType.format(input);

    Object parsed = scalarType.parse(formatToJson);
    assertThat(parsed).isEqualTo(input);
  }

  @Test
  public void parse_withDoubleType_expect_longTypeReturned() {

    ScalarType<?> scalarType = factory.typeFor(Double.class, true);

    List<Double> input = new ArrayList<>();
    input.add(2D);
    input.add(4D);

    String formatToJson = scalarType.format(input);

    Object parsed = scalarType.parse(formatToJson);
    assertThat(parsed).isEqualTo(input);
  }

  @Test
  public void jsonRead_withUuidType() throws IOException {

    ScalarType<?> scalarType = factory.typeFor(UUID.class, true);

    List<UUID> input = new ArrayList<>();
    input.add(UUID.randomUUID());
    input.add(UUID.randomUUID());
    String asJson = EJson.write(input);

    JsonParser parser = DB.json().createParser(new StringReader(asJson));

    Object parsed = scalarType.jsonRead(parser);
    assertThat(parsed).isEqualTo(input);
  }
}
