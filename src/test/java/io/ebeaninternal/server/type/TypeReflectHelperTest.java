package io.ebeaninternal.server.type;

import io.ebean.config.ScalarTypeConverter;
import org.junit.Test;
import org.tests.model.ivo.Money;
import org.tests.model.ivo.Oid;
import org.tests.model.ivo.SysTime;
import org.tests.model.ivo.converter.MoneyTypeConverter;
import org.tests.model.ivo.converter.OidTypeConverter;
import org.tests.model.ivo.converter.SysTimeConverter;

import javax.persistence.AttributeConverter;
import java.math.BigDecimal;
import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeReflectHelperTest {

  @Test
  public void getParams_MoneyTypeConverter() {

    Class<?>[] params = TypeReflectHelper.getParams(MoneyTypeConverter.class, AttributeConverter.class);

    assertThat(params.length).isEqualTo(2);
    assertThat(params[0]).isEqualTo(Money.class);
    assertThat(params[1]).isEqualTo(BigDecimal.class);
  }

  @Test
  public void getParams_OidTypeConverter() {

    Class<?>[] params = TypeReflectHelper.getParams(OidTypeConverter.class, ScalarTypeConverter.class);

    assertThat(params.length).isEqualTo(2);
    assertThat(params[0]).isEqualTo(Oid.class);
    assertThat(params[1]).isEqualTo(Long.class);
  }

  @Test
  public void getParams_SysTimeConverter() {

    Class<?>[] params = TypeReflectHelper.getParams(SysTimeConverter.class, ScalarTypeConverter.class);

    assertThat(params.length).isEqualTo(2);
    assertThat(params[0]).isEqualTo(SysTime.class);
    assertThat(params[1]).isEqualTo(Timestamp.class);
  }

  @Test
  public void getParams_RichTextConverter() {

    Class<?>[] params = TypeReflectHelper.getParams(RichTextConverter.class, ScalarTypeConverter.class);

    assertThat(params.length).isEqualTo(2);
    assertThat(params[0]).isEqualTo(RichText.class);
    assertThat(params[1]).isEqualTo(byte[].class);
  }

  static class RichText {

  }

  static class RichTextConverter extends Direct<RichText> {}

  static class Direct<M>  implements ScalarTypeConverter<M, byte[]> {

    @Override
    public M getNullValue() {
      return null;
    }

    @Override
    public M wrapValue(byte[] scalarType) {
      return null;
    }

    @Override
    public byte[] unwrapValue(M beanType) {
      return new byte[0];
    }
  }

}
