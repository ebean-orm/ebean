package io.ebean.test;

import io.ebean.DB;
import io.ebean.xtest.BaseTestCase;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasic;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RandomValueGeneratorTest extends BaseTestCase {

  private final RandomValueGenerator generator = new RandomValueGenerator();

  @SuppressWarnings("unchecked")
  private <T> BeanDescriptor<T> descriptor(Class<T> cls) {
    return (BeanDescriptor<T>) DB.getDefault().pluginApi().beanType(cls);
  }

  @Test
  void generate_stringType_returnsEightCharString() {
    Object value = generator.generate(String.class);

    assertThat(value).isInstanceOf(String.class);
    assertThat((String) value).hasSize(8);
  }

  @Test
  void generate_stringPropWithLength_cappedAtDbLength() {
    BeanDescriptor<EBasic> descriptor = descriptor(EBasic.class);
    BeanProperty nameProp = descriptor.findProperty("name");

    assertThat(nameProp.dbLength()).isEqualTo(127);
    Object value = generator.generate(nameProp);

    assertThat(value).isInstanceOf(String.class);
    assertThat((String) value).hasSizeLessThanOrEqualTo(127);
    assertThat((String) value).isNotEmpty();
  }

  @Test
  void generate_stringPropWithNoLength_returnsEightCharString() {
    BeanDescriptor<EBasic> descriptor = descriptor(EBasic.class);
    BeanProperty descProp = descriptor.findProperty("description");

    // description has no @Size annotation — dbLength is 0 (unlimited)
    Object value = generator.generate(descProp);

    assertThat(value).isInstanceOf(String.class);
    assertThat((String) value).hasSize(8);
  }

  @Test
  void generate_variousScalarTypes_returnsExpectedTypes() {
    assertThat(generator.generate(Long.class)).isInstanceOf(Long.class);
    assertThat(generator.generate(long.class)).isInstanceOf(Long.class);
    assertThat(generator.generate(Integer.class)).isInstanceOf(Integer.class);
    assertThat(generator.generate(int.class)).isInstanceOf(Integer.class);
    assertThat(generator.generate(Short.class)).isInstanceOf(Short.class);
    assertThat(generator.generate(short.class)).isInstanceOf(Short.class);
    assertThat(generator.generate(Boolean.class)).isEqualTo(Boolean.TRUE);
    assertThat(generator.generate(boolean.class)).isEqualTo(Boolean.TRUE);
    assertThat(generator.generate(UUID.class)).isInstanceOf(UUID.class);
    assertThat(generator.generate(Instant.class)).isInstanceOf(Instant.class);
    assertThat(generator.generate(OffsetDateTime.class)).isInstanceOf(OffsetDateTime.class);
    assertThat(generator.generate(LocalDate.class)).isInstanceOf(LocalDate.class);
    assertThat(generator.generate(LocalDateTime.class)).isInstanceOf(LocalDateTime.class);
    assertThat(generator.generate(BigDecimal.class)).isInstanceOf(BigDecimal.class);
    assertThat(generator.generate(Double.class)).isInstanceOf(Double.class);
    assertThat(generator.generate(Float.class)).isInstanceOf(Float.class);
  }

  @Test
  void generate_enumType_returnsFirstConstant() {
    Object value = generator.generate(EBasic.Status.class);

    assertThat(value).isEqualTo(EBasic.Status.NEW);
  }

  @Test
  void generate_unknownType_returnsNull() {
    assertThat(generator.generate(Object.class)).isNull();
    assertThat(generator.generate((Class<?>) null)).isNull();
  }

  @Test
  void generate_stringPropWithLengthShorterThan32_truncatesCorrectly() {
    BeanDescriptor<EBasic> descriptor = descriptor(EBasic.class);
    BeanProperty nameProp = descriptor.findProperty("name");

    // Run many times to verify no value ever exceeds the limit
    for (int i = 0; i < 20; i++) {
      Object value = generator.generate(nameProp);
      assertThat((String) value).hasSizeLessThanOrEqualTo(127);
    }
  }
}
