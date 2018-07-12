package org.tests.text.json;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.ebean.plugin.BeanType;
import io.ebean.text.json.JsonIOException;
import io.ebean.text.json.JsonVersionMigrationHandler;

/**
 * This is a sample to demonstrate, how JSON migration can work.
 *
 * It tries to invoke static migration routines that are implemented in the bean class.
 *
 * <p>signature of a method: <code>int migrateJson1(ObjectNode, ObjectMapper)</code></p>
 *
 * The method has to return the version number where it has migrated to. All migration methods are called in that order.
 * e.g. if the current version is 15 and you read a json of version 13, it will call migrateJson13 (which migrates to 14),
 * then call migrateJson14 (which migrates to 15)
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public class ExampleJsonVersionMigrationHandler implements JsonVersionMigrationHandler {

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
  public ObjectNode migrateRoot(ObjectNode node, ObjectMapper mapper, BeanType<?> rootBeanType) throws IOException {
    int jsonVersion = node.get("_v") == null ? 1 : node.get("_v").asInt();
    int beanVersion = cache.computeIfAbsent(rootBeanType.getBeanType(), this::getBeanVersion);

    while (jsonVersion != beanVersion) {
      try {
        // version mismatch, try to find "migrateRoot method
        Method migrationMethod = rootBeanType.getBeanType().getMethod("migrateRootJson" + jsonVersion, ObjectNode.class, ObjectMapper.class);
        jsonVersion = (Integer) migrationMethod.invoke(null, node, mapper);
      } catch (NoSuchMethodException nsme) {
        // no migrateRootJsonX method found, so rely we can migrate that in step 2
        return node;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      node.put("_v", jsonVersion);
    }
    return node;
  }

  @Override
  public ObjectNode migrate(ObjectNode node, ObjectMapper mapper, BeanType<?> beanType) throws IOException {
    int jsonVersion = node.get("_v") == null ? 1 : node.get("_v").asInt();
    int beanVersion = cache.computeIfAbsent(beanType.getBeanType(), this::getBeanVersion);

    while (jsonVersion != beanVersion) {
      try {
        // version mismatch, try to find "migrateRoot method
        Method migrationMethod = beanType.getBeanType().getMethod("migrateJson" + jsonVersion, ObjectNode.class, ObjectMapper.class);
        jsonVersion = (Integer) migrationMethod.invoke(null, node, mapper);
      } catch (NoSuchMethodException nsme) {
        throw new JsonIOException("No migration path from " + beanType.getName() + "(v=" + jsonVersion + ")");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      node.put("_v", jsonVersion);
    }
    return node;
  }

}