package io.ebeaninternal.server.persist.platform;

import io.ebean.annotation.Platform;

/**
 * Creates a MultiValueBind for the given platform.
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public class MultiValueBindFactory {

  /**
   * @param platform
   * @return
   */
  public static MultiValueBind from(Platform platform) {
    switch (platform) {
      case POSTGRES:
        return new PostgresMultiValueBind();
      case SQLSERVER16:
      case SQLSERVER17:
      case SQLSERVER:
        return new SqlServerMultiValueBind();
      case ORACLE:
        return new OracleMultiValueBind();
      default:
        return new MultiValueBind();
    }
  }

}
