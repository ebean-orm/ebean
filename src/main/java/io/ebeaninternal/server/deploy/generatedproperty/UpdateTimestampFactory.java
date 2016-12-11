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
 * Helper for creating Update timestamp GeneratedProperty objects.
 */
public class UpdateTimestampFactory {

  final GeneratedUpdateLong longTime = new GeneratedUpdateLong();

  final Map<Class<?>, GeneratedProperty> map = new HashMap<>();

  public UpdateTimestampFactory(ClassLoadConfig classLoadConfig) {
    map.put(Timestamp.class, new GeneratedUpdateTimestamp());
    map.put(java.util.Date.class, new GeneratedUpdateDate());
    map.put(Long.class, longTime);
    map.put(long.class, longTime);

    if (classLoadConfig.isJavaTimePresent()) {
      map.put(Instant.class, new GeneratedUpdateJavaTime.InstantDT());
      map.put(LocalDateTime.class, new GeneratedUpdateJavaTime.LocalDT());
      map.put(OffsetDateTime.class, new GeneratedUpdateJavaTime.OffsetDT());
      map.put(ZonedDateTime.class, new GeneratedUpdateJavaTime.ZonedDT());
    }
    if (classLoadConfig.isJodaTimePresent()) {
      map.put(org.joda.time.LocalDateTime.class, new GeneratedUpdateJodaTime.LocalDT());
      map.put(org.joda.time.DateTime.class, new GeneratedUpdateJodaTime.DateTimeDT());
    }
  }

  public void setUpdateTimestamp(DeployBeanProperty property) {

    property.setGeneratedProperty(createUpdateTimestamp(property));
  }

  /**
   * Create the update GeneratedProperty depending on the property type.
   */
  protected GeneratedProperty createUpdateTimestamp(DeployBeanProperty property) {

    Class<?> propType = property.getPropertyType();
    GeneratedProperty generatedProperty = map.get(propType);
    if (generatedProperty != null) {
      return generatedProperty;
    }

    throw new PersistenceException("Generated update Timestamp not supported on " + propType.getName());
  }

}
