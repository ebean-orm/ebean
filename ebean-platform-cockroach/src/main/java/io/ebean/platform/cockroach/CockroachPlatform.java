package io.ebean.platform.cockroach;

import io.ebean.annotation.Platform;
import io.ebean.platform.postgres.PostgresPlatform;

/**
 * CockroachDB based platform.
 */
public class CockroachPlatform extends PostgresPlatform {

  public CockroachPlatform() {
    super();
    this.platform = Platform.COCKROACH;
    this.historySupport = null; // not yet implemented in DDL
  }

}
