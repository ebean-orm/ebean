package io.ebeaninternal.server.type;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ebean.ModifyAwareType;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.dbplatform.DbPlatformType;

class TypeJsonManager {

  interface DirtyHandler {
    boolean isDirty(Object value);
  }

  private final boolean postgres;
  private final ObjectMapper objectMapper;
  private final DirtyHandler defaultHandler;
  private final DirtyHandler modifyAwareHandler;

  TypeJsonManager(boolean postgres, Object objectMapper, boolean defaultDirty) {
    this.postgres = postgres;
    this.objectMapper = (ObjectMapper) objectMapper;
    this.defaultHandler = new DefaultHandler(defaultDirty);
    this.modifyAwareHandler = new ModifyAwareHandler();
  }

  ObjectMapper objectMapper() {
    return objectMapper;
  }

  String postgresType(int dbType) {
    if (postgres) {
      switch (dbType) {
        case DbPlatformType.JSON:
          return PostgresHelper.JSON_TYPE;
        case DbPlatformType.JSONB:
          return PostgresHelper.JSONB_TYPE;
      }
    }
    return null;
  }

  /**
   * Return the DirtyHandler to use.
   */
  DirtyHandler dirtyHandler(Class<?> cls, Class<?> rawType) {
    if (!Object.class.equals(cls) || ModifyAwareType.class.isAssignableFrom(rawType)) {
      // Set, List and Map are modify aware
      return modifyAwareHandler;
    }
    return defaultHandler;
  }

  /**
   * Return true if the value should be considered dirty (and included in an update).
   */
  static boolean checkIsDirty(Object value) {
    if (value instanceof ModifyAwareType) {
      return checkModifyAware(value);
    }
    return true;
  }

  private static boolean checkModifyAware(Object value) {
    ModifyAwareType modifyAware = (ModifyAwareType) value;
    if (modifyAware.isMarkedDirty()) {
      // reset the dirty state (consider not dirty after update)
      modifyAware.setMarkedDirty(false);
      return true;
    } else {
      return false;
    }
  }

  static final class ModifyAwareHandler implements DirtyHandler {
    @Override
    public boolean isDirty(Object value) {
      return checkModifyAware(value);
    }
  }

  /**
   * Effectively constant based on {@link DatabaseConfig#isJsonDirtyByDefault()}
   */
  static final class DefaultHandler implements DirtyHandler {

    private final boolean dirty;

    DefaultHandler(boolean dirty) {
      this.dirty = dirty;
    }

    @Override
    public boolean isDirty(Object value) {
      return dirty;
    }
  }

}
