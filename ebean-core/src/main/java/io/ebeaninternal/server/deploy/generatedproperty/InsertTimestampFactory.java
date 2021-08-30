package io.ebeaninternal.server.deploy.generatedproperty;

import io.ebean.config.ClassLoadConfig;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;

import javax.persistence.PersistenceException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper for creating Insert timestamp GeneratedProperty objects.
 */
final class InsertTimestampFactory {

  private final Map<Class<?>, GeneratedProperty> map = new HashMap<>();

  InsertTimestampFactory(ClassLoadConfig classLoadConfig) {
    map.put(Timestamp.class, new GeneratedInsertTimestamp());
    map.put(java.util.Date.class, new GeneratedInsertDate());
    GeneratedInsertLong longTime = new GeneratedInsertLong();
    map.put(Long.class, longTime);
    map.put(long.class, longTime);
    map.put(Instant.class, new GeneratedInsertJavaTime.InstantDT());
    map.put(LocalDateTime.class, new GeneratedInsertJavaTime.LocalDT());
    map.put(OffsetDateTime.class, new GeneratedInsertJavaTime.OffsetDT());
    map.put(ZonedDateTime.class, new GeneratedInsertJavaTime.ZonedDT());
    if (classLoadConfig.isJodaTimePresent()) {
      map.put(org.joda.time.LocalDateTime.class, new GeneratedInsertJodaTime.LocalDT());
      map.put(org.joda.time.DateTime.class, new GeneratedInsertJodaTime.DateTimeDT());
    }
  }

  void setInsertTimestamp(DeployBeanProperty property) {
    property.setGeneratedProperty(createInsertTimestamp(property));
  }

  /**
   * Create the insert GeneratedProperty depending on the property type.
   */
  GeneratedProperty createInsertTimestamp(DeployBeanProperty property) {
    Class<?> propType = property.getPropertyType();
    GeneratedProperty generatedProperty = map.get(propType);
    if (generatedProperty != null) {
      return generatedProperty;
    }
    throw new PersistenceException("Generated Insert Timestamp not supported on " + propType.getName());
  }

}
