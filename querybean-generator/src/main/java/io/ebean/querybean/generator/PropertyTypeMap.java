package io.ebean.querybean.generator;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Holds the Property types and how they match to class types.
 */
class PropertyTypeMap {

  /**
   * Property type for Db Json properties.
   */
  private final PropertyType dbJsonType = new PropertyType("PJson");

  private Map<String,PropertyType> map = new HashMap<>();

  PropertyTypeMap() {

    map.put("boolean", new PropertyType("PBoolean"));
    map.put("short", new PropertyType("PShort"));
    map.put("int", new PropertyType("PInteger"));
    map.put("long", new PropertyType("PLong"));
    map.put("double", new PropertyType("PDouble"));
    map.put("float", new PropertyType("PFloat"));

    addType(Boolean.class);
    addType(Short.class);
    addType(Integer.class);
    addType(Long.class);
    addType(Double.class);
    addType(Float.class);
    addType(String.class);
    addType(Timestamp.class);
    map.put(java.sql.Date.class.getName(), new PropertyType("PSqlDate"));
    map.put(java.util.Date.class.getName(), new PropertyType("PUtilDate"));
    addType(java.sql.Time.class);
    addType(TimeZone.class);
    addType(BigDecimal.class);
    addType(BigInteger.class);
    addType(Calendar.class);
    addType(Currency.class);
    addType(Class.class);
    addType(Locale.class);
    map.put("java.lang.Class<?>", new PropertyType("PClass"));
    addType(File.class);
    addType(InetAddress.class);

    map.put(URI.class.getName(), new PropertyType("PUri"));
    map.put(URL.class.getName(), new PropertyType("PUrl"));
    map.put(UUID.class.getName(), new PropertyType("PUuid"));
    map.put("io.ebean.types.Inet", new PropertyType("PInet"));
    map.put("io.ebean.types.Cidr", new PropertyType("PCidr"));
    addJava8Types();
    addJodaTypes();
  }

  private void addType(Class<?> cls) {
    String simpleName = cls.getSimpleName();
    map.put(cls.getName(), new PropertyType("P"+simpleName));
  }

  private void addJava8Types() {

    try {
      Class.forName("java.time.Instant");
    } catch (ClassNotFoundException e) {
      return;
    }
    addType(java.time.DayOfWeek.class);
    addType(java.time.Duration.class);
    addType(java.time.Instant.class);
    addType(java.time.LocalDate.class);
    addType(java.time.LocalDateTime.class);
    addType(java.time.LocalTime.class);
    addType(java.time.Month.class);
    addType(java.time.MonthDay.class);
    addType(java.time.OffsetDateTime.class);
    addType(java.time.OffsetTime.class);
    addType(java.time.Year.class);
    addType(java.time.YearMonth.class);
    addType(java.time.ZoneId.class);
    addType(java.time.ZoneOffset.class);
  }

  private void addJodaTypes() {
    map.put("org.joda.time.DateTime", new PropertyType("PJodaDateTime"));
    map.put("org.joda.time.DateMidnight", new PropertyType("PJodaDateMidnight"));
    map.put("org.joda.time.LocalDate", new PropertyType("PJodaLocalDate"));
    map.put("org.joda.time.LocalDateTime", new PropertyType("PJodaLocalDateTime"));
    map.put("org.joda.time.LocalTime", new PropertyType("PJodaLocalTime"));
  }

  /**
   * Return the property type for the given class description.
   */
  PropertyType getType(String classDesc) {
    return map.get(classDesc);
  }

  /**
   * Return the Db Json property type (for DbJson and DbJsonB).
   */
  PropertyType getDbJsonType() {
    return dbJsonType;
  }
}
