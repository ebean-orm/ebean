package io.ebean.config.dbplatform.postgres;

import io.ebean.annotation.Platform;

/**
 * Postgres9 platform - uses serial type for identity columns.
 */
public class Postgres9Platform extends PostgresPlatform {

  public Postgres9Platform() {
    super();
    this.platform = Platform.POSTGRES9;
  }
}
