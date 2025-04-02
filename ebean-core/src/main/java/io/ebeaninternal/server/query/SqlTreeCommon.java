package io.ebeaninternal.server.query;

import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.TableJoin;

final class SqlTreeCommon {

  private final SpiQuery.TemporalMode temporalMode;
  private final boolean disableLazyLoad;
  private final boolean unmodifiable;
  private final TableJoin includeJoin;

  SqlTreeCommon(SpiQuery.TemporalMode temporalMode, boolean disableLazyLoad, boolean unmodifiable, TableJoin includeJoin) {
    this.temporalMode = temporalMode;
    this.disableLazyLoad = disableLazyLoad;
    this.unmodifiable = unmodifiable;
    this.includeJoin = includeJoin;
  }

  SpiQuery.TemporalMode temporalMode() {
    return temporalMode;
  }

  boolean disableLazyLoad() {
    return disableLazyLoad;
  }

  boolean unmodifiable() {
    return unmodifiable;
  }

  TableJoin includeJoin() {
    return includeJoin;
  }

}
