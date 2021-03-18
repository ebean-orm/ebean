package io.ebeaninternal.server.type;

import org.junit.Test;

import javax.persistence.AttributeConverter;

import static org.assertj.core.api.Assertions.assertThat;


@SuppressWarnings({"rawtypes", "unchecked"})
public class ScalarTypeWrapperAdapterTest {

  private final ScalarTypeString stringType = ScalarTypeString.INSTANCE;
  private final MyAdapter myAdapter = new MyAdapter();
  private final AttributeConverterAdapter converterAdapter = new AttributeConverterAdapter(myAdapter);
  private final ScalarTypeWrapper<Long, String> wrapper = new ScalarTypeWrapper(Long.class, stringType, converterAdapter);

  @Test
  public void toJdbcType() {
    assertThat(wrapper.toJdbcType(42L)).isEqualTo("L42");
    assertThat(wrapper.toJdbcType(93L)).isEqualTo("L93");
  }

  @Test
  public void toJdbcType_when_nullValue() {
    assertThat(wrapper.toJdbcType(MyAdapter.NULL_VAL)).isNull();
  }

  @Test
  public void toBeanType_when_null_expect_customNullValue() {
    assertThat(wrapper.toBeanType(null)).isEqualTo(MyAdapter.NULL_VAL);
  }

  @Test
  public void toBeanType() {
    assertThat(wrapper.toBeanType("L34")).isEqualTo(34L);
  }

  /**
   * An AttributeConverter with a custom null value (of -1L).
   */
  private static class MyAdapter implements AttributeConverter<Long, String> {

    private static final Long NULL_VAL = -1L;

    @Override
    public String convertToDatabaseColumn(Long val) {
      if (val == null || val.equals(NULL_VAL)) {
        return null;
      }
      return "L" + val;
    }

    @Override
    public Long convertToEntityAttribute(String dbData) {
      if (dbData == null) {
        return NULL_VAL;
      }
      return Long.parseLong(dbData.substring(1));
    }
  }
}
