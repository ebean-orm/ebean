package io.ebeaninternal.server.query;

import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.TableJoin;

final class SqlTreeCommon {

  private final SpiQuery.TemporalMode temporalMode;
  private final boolean disableLazyLoad;
  private final boolean readOnly;
  private final TableJoin includeJoin;

  SqlTreeCommon(SpiQuery.TemporalMode temporalMode, boolean disableLazyLoad, boolean readOnly, TableJoin includeJoin) {
    this.temporalMode = temporalMode;
    this.disableLazyLoad = disableLazyLoad;
    this.readOnly = readOnly;
    this.includeJoin = includeJoin;
  }

  SpiQuery.TemporalMode temporalMode() {
    return temporalMode;
  }

  boolean disableLazyLoad() {
    return disableLazyLoad;
  }

  boolean readOnly() {
    return readOnly;
  }

  TableJoin includeJoin() {
    return includeJoin;
  }

}
