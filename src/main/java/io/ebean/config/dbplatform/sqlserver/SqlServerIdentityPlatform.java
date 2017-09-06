package io.ebean.config.dbplatform.sqlserver;

import io.ebean.PersistBatch;
import io.ebean.config.dbplatform.IdType;

/**
 * SqlServerPlatform that uses IDENTITY instead of SEQUENCE.
 *  
 * @deprecated Use this for backwards compatibility only.
 * @author Roland Praml, FOCONIS AG
 */
public class SqlServerIdentityPlatform extends SqlServerPlatform {

  public SqlServerIdentityPlatform() {
    super();
    this.persistBatchOnCascade = PersistBatch.NONE;
    this.dbIdentity.setIdType(IdType.IDENTITY);
    this.dbIdentity.setSupportsGetGeneratedKeys(true);
    this.dbIdentity.setSupportsIdentity(true);
  }
}
