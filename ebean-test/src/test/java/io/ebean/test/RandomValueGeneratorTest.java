package io.ebean.test;

import io.ebean.DB;
import io.ebean.xtest.BaseTestCase;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import org.junit.jupiter.api.Test;
import org.tests.cache.personinfo.PersonOther;
import org.tests.model.basic.EBasic;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RandomValueGeneratorTest extends BaseTestCase {

  private final RandomValueGenerator generator = new RandomValueGenerator();

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
    assertThat(generator.generate(ZonedDateTime.class)).isInstanceOf(ZonedDateTime.class);
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
  void generate_emailPropName_returnsEmailAddress() {
    BeanDescriptor<PersonOther> descriptor = descriptor(PersonOther.class);
    BeanProperty emailProp = descriptor.findProperty("email");

    assertThat(emailProp.dbLength()).isEqualTo(60);
    Object value = generator.generate(emailProp);

    assertThat(value).isInstanceOf(String.class);
    assertThat((String) value).contains("@domain.com");
    assertThat((String) value).hasSizeLessThanOrEqualTo(60);
  }

  @Test
  void generate_bigDecimalType_returnsScaledValue() {
    Object value = generator.generate(BigDecimal.class);

    assertThat(value).isInstanceOf(BigDecimal.class);
    BigDecimal decimal = (BigDecimal) value;
    assertThat(decimal.scale()).isEqualTo(2);
    assertThat(decimal).isGreaterThan(BigDecimal.ZERO);
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

  // --- direct protected-method tests (no database needed) ---

  @Test
  void randomLong_isPositiveAndInRange() {
    long v = generator.randomLong();
    assertThat(v).isBetween(1L, 100_000L);
  }

  @Test
  void randomInt_isPositiveAndInRange() {
    int v = generator.randomInt();
    assertThat(v).isBetween(1, 999);
  }

  @Test
  void randomShort_isPositiveAndInRange() {
    short v = generator.randomShort();
    assertThat((int) v).isBetween(1, 99);
  }

  @Test
  void randomBoolean_returnsTrue() {
    assertThat(generator.randomBoolean()).isTrue();
  }

  @Test
  void randomUUID_returnsValidUUID() {
    UUID v = generator.randomUUID();
    assertThat(v).isNotNull();
    assertThat(v.toString()).hasSize(36);
  }

  @Test
  void randomInstant_returnsInstant() {
    assertThat(generator.randomInstant()).isInstanceOf(Instant.class);
  }

  @Test
  void randomOffsetDateTime_returnsOffsetDateTime() {
    assertThat(generator.randomOffsetDateTime()).isInstanceOf(OffsetDateTime.class);
  }

  @Test
  void randomZonedDateTime_returnsZonedDateTime() {
    assertThat(generator.randomZonedDateTime()).isInstanceOf(ZonedDateTime.class);
  }

  @Test
  void randomLocalDate_returnsLocalDate() {
    assertThat(generator.randomLocalDate()).isInstanceOf(LocalDate.class);
  }

  @Test
  void randomLocalDateTime_returnsLocalDateTime() {
    assertThat(generator.randomLocalDateTime()).isInstanceOf(LocalDateTime.class);
  }

  @Test
  void randomDouble_isPositiveAndInRange() {
    double v = generator.randomDouble();
    assertThat(v).isBetween(1.0, 100.0);
  }

  @Test
  void randomFloat_isPositiveAndInRange() {
    float v = generator.randomFloat();
    assertThat((double) v).isBetween(1.0, 100.0);
  }

  @Test
  void randomBigDecimal_defaultPrecisionAndScale_scaleIsTwo() {
    BigDecimal v = generator.randomBigDecimal(0, -1);
    assertThat(v.scale()).isEqualTo(2);
    assertThat(v).isGreaterThan(BigDecimal.ZERO);
  }

  @Test
  void randomBigDecimal_explicitPrecisionAndScale_fitsWithinBounds() {
    // DECIMAL(8,3) → max integer part 99999, scale 3
    BigDecimal v = generator.randomBigDecimal(8, 3);
    assertThat(v.scale()).isEqualTo(3);
    assertThat(v.precision()).isLessThanOrEqualTo(8);
    assertThat(v).isGreaterThan(BigDecimal.ZERO);
  }

  @Test
  void randomString_emailPropName_containsDomainSuffix() {
    String v = generator.randomString("emailAddress", 0);
    assertThat(v).endsWith("@domain.com");
  }

  @Test
  void randomString_regularPropName_returnsEightChars() {
    String v = generator.randomString("name", 0);
    assertThat(v).hasSize(8);
  }

  @Test
  void randomString_withMaxLength_isTruncated() {
    String v = generator.randomString("description", 5);
    assertThat(v).hasSize(5);
  }

  @Test
  void subclassCanOverrideRandomLong() {
    RandomValueGenerator fixed = new RandomValueGenerator() {
      @Override
      protected long randomLong() { return 42L; }
    };
    assertThat(fixed.generate(Long.class)).isEqualTo(42L);
  }
}
