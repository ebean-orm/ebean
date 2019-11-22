package org.tests.text.json;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.ebean.plugin.BeanType;
import io.ebean.text.json.JsonVersionWriter;
import io.ebean.text.json.JsonWriter;

/**
 * This is a very simple JsonVersionMigrationHander that writes the static value of JSON_VERSION if present (else write 1).
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public class ExampleJsonVersionWriter implements JsonVersionWriter {

  private Map<Class<?>, Integer> cache = new ConcurrentHashMap<>();

  private Integer getBeanVersion(Class<?> cls) {
    try {
      Field version = cls.getDeclaredField("JSON_VERSION");
      version.setAccessible(true);
      return version.getInt(null);
    } catch (NoSuchFieldException e) {
      return 1; // default version is 1
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void writeVersion(JsonWriter writer, BeanType<?> beanType) {
    writer.writeNumberField("_v", cache.computeIfAbsent(beanType.getBeanType(), this::getBeanVersion));
  }
}