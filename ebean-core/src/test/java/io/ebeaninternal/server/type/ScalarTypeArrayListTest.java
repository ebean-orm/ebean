package io.ebeaninternal.server.type;


import io.ebean.core.type.DataReader;
import io.ebean.core.type.ScalarType;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.UUID;

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

}
