package io.ebeaninternal.server.type;

import org.junit.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ScalarTypeArraySetTest extends BasePlatformArrayTypeFactoryTest {

  private final PlatformArrayTypeFactory factory = ScalarTypeArraySet.factory();

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
