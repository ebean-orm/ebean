package io.ebeaninternal.server.type;

import org.junit.Test;
import org.tests.model.ivo.Oid;
import org.tests.model.ivo.converter.OidTypeConverter;

import static org.assertj.core.api.Assertions.assertThat;


@SuppressWarnings({"rawtypes", "unchecked"})
public class ScalarTypeWrapperOidTest {

  private final OidTypeConverter oidTypeConverter = new OidTypeConverter();
  private final ScalarTypeLong longType = new ScalarTypeLong();
  private final ScalarTypeWrapper<Oid<?>,Long> wrapper = new ScalarTypeWrapper(Oid.class, longType, oidTypeConverter);

  @Test
  public void toJdbcType() {

    assertThat(wrapper.toJdbcType(new Oid<String>(42))).isEqualTo(42L);
    assertThat(wrapper.toJdbcType(new Oid<String>(98))).isEqualTo(98L);
  }

  @Test
  public void toJdbcType_when_nullValue() {
    assertThat(wrapper.toJdbcType(OidTypeConverter.NULL_VALUE)).isNull();
  }

}
