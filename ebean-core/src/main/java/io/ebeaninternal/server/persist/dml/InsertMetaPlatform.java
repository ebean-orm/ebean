package io.ebeaninternal.server.persist.dml;

import io.ebean.InsertOptions;
import io.ebean.annotation.Platform;
import io.ebeaninternal.server.deploy.BeanDescriptor;

final class InsertMetaPlatform {

  private static final NotSupported NOT_SUPPORTED = new NotSupported();

  static InsertMetaOptions create(Platform platform, BeanDescriptor<?> desc, InsertMeta meta) {
    switch (platform.base()) {
      case POSTGRES:
      case YUGABYTE:
      case COCKROACH:
        return new InsertMetaOptionsPostgres(meta, desc);
      default:
        return NOT_SUPPORTED;
    }
  }

  static final class NotSupported implements InsertMetaOptions {
    @Override
    public String sql(boolean withId, InsertOptions insertOptions) {
      throw new UnsupportedOperationException("InsertOptions not supported on this database platform");
    }
  }

}
