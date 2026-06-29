package io.ebeaninternal.server.type;

import io.ebean.config.ScalarTypeConverter;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.ivo.Money;
import org.tests.model.ivo.Oid;
import org.tests.model.ivo.SysTime;
import org.tests.model.ivo.converter.MoneyTypeConverter;
import org.tests.model.ivo.converter.OidTypeConverter;
import org.tests.model.ivo.converter.SysTimeConverter;

import jakarta.persistence.AttributeConverter;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    // note: values are accessed via reflection
    @SuppressWarnings("unused")
    List<? extends Order.Status> wildOrderStatus = new ArrayList<>();

    @SuppressWarnings("unused")
    List<Order.Status> orderStatus = new ArrayList<>();
  }

  // --- resolveToClass ---

  @Test
  void resolveToClass_rawClass() {
    assertThat(TypeReflectHelper.resolveToClass(String.class)).isEqualTo(String.class);
  }

  @Test
  void resolveToClass_parameterizedType() throws NoSuchFieldException {
    Field f = Some.class.getDeclaredField("orderStatus");
    assertThat(TypeReflectHelper.resolveToClass(f.getGenericType())).isEqualTo(List.class);
  }

  @Test
  void resolveToClass_wildcardType() throws NoSuchFieldException {
    // List<? extends TestEnum> → wildcard upper bound = TestEnum
    Field f = GenericFixtures.class.getDeclaredField("wildEnums");
    Type wildcard = ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];
    assertThat(TypeReflectHelper.resolveToClass(wildcard)).isEqualTo(TestEnum.class);
  }

  @Test
  void resolveToClass_unknown_returnsNull() throws NoSuchFieldException {
    // A bare TypeVariable has no binding → should return null, not throw
    Field f = GenericBase.class.getDeclaredField("value");
    Type typeVar = f.getGenericType(); // TypeVariable T
    assertThat(TypeReflectHelper.resolveToClass(typeVar)).isNull();
  }

  // --- resolveCollectionTarget ---

  @Test
  void resolveCollectionTarget_rawClass() {
    assertThat(TypeReflectHelper.resolveCollectionTarget(String.class)).isEqualTo(String.class);
  }

  @Test
  void resolveCollectionTarget_enumClass() throws NoSuchFieldException {
    // List<TestEnum> → element type arg is TestEnum (plain Class)
    Field f = GenericFixtures.class.getDeclaredField("enums");
    ParameterizedType listType = (ParameterizedType) f.getGenericType();
    Type arg = listType.getActualTypeArguments()[0];
    assertThat(TypeReflectHelper.resolveCollectionTarget(arg)).isEqualTo(TestEnum.class);
  }

  @Test
  void resolveCollectionTarget_nestedParameterizedType() throws NoSuchFieldException {
    // List<List<String>> → element type arg is List<String>; raw = List.class
    Field f = Nested.class.getDeclaredField("matrix");
    ParameterizedType outer = (ParameterizedType) f.getGenericType();
    Type inner = outer.getActualTypeArguments()[0]; // List<String> — a ParameterizedType
    assertThat(TypeReflectHelper.resolveCollectionTarget(inner)).isEqualTo(List.class);
  }

  // --- resolveType + typeVariableMap: single-level generic superclass ---

  @Test
  void resolveType_singleLevel_typeVariable() throws NoSuchFieldException {
    Map<TypeVariable<?>, Type> typeMap = TypeReflectHelper.typeVariableMap(SingleLevelConcrete.class);
    Field field = GenericBase.class.getDeclaredField("value");

    Type resolved = TypeReflectHelper.resolveType(field.getGenericType(), typeMap);

    assertThat(TypeReflectHelper.resolveToClass(resolved)).isEqualTo(String.class);
  }

  @Test
  void resolveType_singleLevel_collection() throws NoSuchFieldException {
    Map<TypeVariable<?>, Type> typeMap = TypeReflectHelper.typeVariableMap(SingleLevelConcrete.class);
    Field field = GenericBase.class.getDeclaredField("values");

    Type resolved = TypeReflectHelper.resolveType(field.getGenericType(), typeMap);

    assertThat(TypeReflectHelper.resolveToClass(resolved)).isEqualTo(List.class);
    assertThat(((ParameterizedType) resolved).getActualTypeArguments()[0]).isEqualTo(String.class);
  }

  @Test
  void resolveType_singleLevel_setCollection() throws NoSuchFieldException {
    Map<TypeVariable<?>, Type> typeMap = TypeReflectHelper.typeVariableMap(SingleLevelConcrete.class);
    Field field = GenericBase.class.getDeclaredField("valueSet");

    Type resolved = TypeReflectHelper.resolveType(field.getGenericType(), typeMap);

    assertThat(TypeReflectHelper.resolveToClass(resolved)).isEqualTo(Set.class);
    assertThat(((ParameterizedType) resolved).getActualTypeArguments()[0]).isEqualTo(String.class);
  }

  // --- resolveType + typeVariableMap: multi-level generic hierarchy (bug regression) ---

  @Test
  void resolveType_multiLevel_typeVariable() throws NoSuchFieldException {
    // MultiLevelConcrete extends GenericMiddle<Long> extends GenericBase<T>
    // Before fix: GenericBase.T was unresolved when walking from GenericMiddle → GenericBase
    Map<TypeVariable<?>, Type> typeMap = TypeReflectHelper.typeVariableMap(MultiLevelConcrete.class);
    Field field = GenericBase.class.getDeclaredField("value");

    Type resolved = TypeReflectHelper.resolveType(field.getGenericType(), typeMap);

    assertThat(TypeReflectHelper.resolveToClass(resolved))
      .as("GenericBase.T must resolve to Long through two levels of generic inheritance")
      .isEqualTo(Long.class);
  }

  @Test
  void resolveType_multiLevel_collection() throws NoSuchFieldException {
    Map<TypeVariable<?>, Type> typeMap = TypeReflectHelper.typeVariableMap(MultiLevelConcrete.class);
    Field field = GenericBase.class.getDeclaredField("values");

    Type resolved = TypeReflectHelper.resolveType(field.getGenericType(), typeMap);

    assertThat(TypeReflectHelper.resolveToClass(resolved)).isEqualTo(List.class);
    assertThat(((ParameterizedType) resolved).getActualTypeArguments()[0])
      .as("List element type must resolve to Long through two levels of generic inheritance")
      .isEqualTo(Long.class);
  }

  @Test
  void resolveType_nonGenericField_unchanged() throws NoSuchFieldException {
    Map<TypeVariable<?>, Type> typeMap = TypeReflectHelper.typeVariableMap(MultiLevelConcrete.class);
    Field field = GenericBase.class.getDeclaredField("name");

    Type resolved = TypeReflectHelper.resolveType(field.getGenericType(), typeMap);

    assertThat(resolved).isEqualTo(String.class);
  }

  // --- fixtures ---

  /** Single-level generic base: one TypeVariable used as field type and collection element. */
  private static class GenericBase<T> {
    @SuppressWarnings("unused") T value;
    @SuppressWarnings("unused") List<T> values;
    @SuppressWarnings("unused") Set<T> valueSet;
    @SuppressWarnings("unused") String name;
  }

  /** Concrete subclass - one level: T = String. */
  private static class SingleLevelConcrete extends GenericBase<String> {}

  /** Intermediate generic class - two levels deep: T is still unresolved here. */
  private static class GenericMiddle<T> extends GenericBase<T> {}

  /** Concrete subclass - two levels: T = Long, resolved through GenericMiddle. */
  private static class MultiLevelConcrete extends GenericMiddle<Long> {}

  private static class Nested {
    @SuppressWarnings("unused") List<List<String>> matrix;
  }

  private enum TestEnum { A, B, C }

  private static class GenericFixtures {
    @SuppressWarnings("unused") List<? extends TestEnum> wildEnums;
    @SuppressWarnings("unused") List<TestEnum> enums;
  }

  private static class RichText {}

  private static class RichTextConverter extends Direct<RichText> {}

  private static class Direct<M> implements ScalarTypeConverter<M, byte[]> {

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
