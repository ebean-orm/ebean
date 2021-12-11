package io.ebeaninternal.server.type;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ebean.ModifyAwareType;
import io.ebean.annotation.MutationDetection;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;

final class TypeJsonManager {

  private final boolean postgres;
  private final ObjectMapper objectMapper;
  private final MutationDetection mutationDetection;

  TypeJsonManager(boolean postgres, Object objectMapper, MutationDetection mutationDetection) {
    this.postgres = postgres;
    this.objectMapper = (ObjectMapper) objectMapper;
    this.mutationDetection = mutationDetection;
  }

  MutationDetection mutationDetection() {
    return mutationDetection;
  }

  ObjectMapper objectMapper() {
    return objectMapper;
  }

  boolean keepSource(DeployBeanProperty prop) {
    if (prop.getMutationDetection() == MutationDetection.SOURCE) {
      return true;
    } else if (prop.getMutationDetection() == MutationDetection.DEFAULT) {
      prop.setMutationDetection(mutationDetection);
      return mutationDetection == MutationDetection.SOURCE;
    } else {
      return false;
    }
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

}
