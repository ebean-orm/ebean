package io.ebeaninternal.server.type;

import io.ebean.config.ScalarTypeConverter;
import org.junit.Test;
import org.tests.model.basic.Order;
import org.tests.model.ivo.Money;
import org.tests.model.ivo.Oid;
import org.tests.model.ivo.SysTime;
import org.tests.model.ivo.converter.MoneyTypeConverter;
import org.tests.model.ivo.converter.OidTypeConverter;
import org.tests.model.ivo.converter.SysTimeConverter;

import javax.persistence.AttributeConverter;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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

  @Test
  public void isEnumType_wildCard() throws NoSuchFieldException {

    Field wildOrderStatus = Some.class.getDeclaredField("wildOrderStatus");
    assertThat(TypeReflectHelper.isEnumType(getValueType(wildOrderStatus.getGenericType()))).isTrue();

    Class<?> aClass = TypeReflectHelper.asEnumClass(getValueType(wildOrderStatus.getGenericType()));
    assertThat(aClass).isEqualTo(Order.Status.class);
  }


  @Test
  public void isEnumType_simpleType() throws NoSuchFieldException {

    Field orderStatus = Some.class.getDeclaredField("orderStatus");

    assertThat(TypeReflectHelper.isEnumType(getValueType(orderStatus.getGenericType()))).isTrue();

    Class<? extends Enum> aClass = TypeReflectHelper.asEnumClass(getValueType(orderStatus.getGenericType()));
    assertThat(aClass).isEqualTo(Order.Status.class);
  }

  @Test
  public void getValueType_simpleType() throws NoSuchFieldException {

    Field orderStatus = Some.class.getDeclaredField("orderStatus");

    Type expected = getValueType(orderStatus.getGenericType());
    assertThat(TypeReflectHelper.getValueType(orderStatus.getGenericType())).isEqualTo(expected);
  }

  @Test
  public void getValueType_wildcardType() throws NoSuchFieldException {

    Field orderStatus = Some.class.getDeclaredField("wildOrderStatus");

    Type expected = getValueType(orderStatus.getGenericType());
    assertThat(TypeReflectHelper.getValueType(orderStatus.getGenericType())).isEqualTo(expected);
  }

  private Type getValueType(Type genericType) {
    return ((ParameterizedType) genericType).getActualTypeArguments()[0];
  }

  private static class Some {

    List<? extends Order.Status> wildOrderStatus = new ArrayList<>();
    List<Order.Status> orderStatus = new ArrayList<>();
  }

  private static class RichText {

  }

  private static class RichTextConverter extends Direct<RichText> {}

  private static class Direct<M>  implements ScalarTypeConverter<M, byte[]> {

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
