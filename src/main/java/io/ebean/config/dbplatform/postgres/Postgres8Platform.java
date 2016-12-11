package io.ebean.config.dbplatform.postgres;

import io.ebean.config.dbplatform.IdType;

/**
 * Postgres v8.3 specific platform.
 * <p>
 * No support for getGeneratedKeys.
 * </p>
 */
public class Postgres8Platform extends PostgresPlatform {

  public Postgres8Platform() {
    super();
    this.dbIdentity.setSupportsGetGeneratedKeys(false);
    this.dbIdentity.setIdType(IdType.SEQUENCE);
    this.dbIdentity.setSupportsSequence(true);
    this.dbIdentity.setSupportsIdentity(true);
    this.columnAliasPrefix = "as c";
  }

}
