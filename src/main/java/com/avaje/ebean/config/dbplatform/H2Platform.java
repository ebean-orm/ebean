package com.avaje.ebean.config.dbplatform;

import com.avaje.ebean.BackgroundExecutor;
import com.avaje.ebean.config.GlobalProperties;

import javax.sql.DataSource;

/**
 * H2 specific platform.
 */
public class H2Platform extends DatabasePlatform {

  public H2Platform() {
    super();
    this.name = "h2";
    this.dbEncrypt = new H2DbEncrypt();

    // only support getGeneratedKeys with non-batch JDBC
    // so generally use SEQUENCE instead of IDENTITY for H2
    boolean useIdentity = GlobalProperties.getBoolean("ebean.h2platform.useIdentity", false);

    IdType idType = useIdentity ? IdType.IDENTITY : IdType.SEQUENCE;
    this.dbIdentity.setIdType(idType);

    this.dbIdentity.setSupportsGetGeneratedKeys(true);
    this.dbIdentity.setSupportsSequence(true);
    this.dbIdentity.setSupportsIdentity(true);

    this.openQuote = "\"";
    this.closeQuote = "\"";

    // H2 data types match default JDBC types
    // so no changes to dbTypeMap required

    this.dbDdlSyntax.setDropIfExists("if exists");
    this.dbDdlSyntax.setDisableReferentialIntegrity("SET REFERENTIAL_INTEGRITY FALSE");
    this.dbDdlSyntax.setEnableReferentialIntegrity("SET REFERENTIAL_INTEGRITY TRUE");
    this.dbDdlSyntax.setForeignKeySuffix("on delete restrict on update restrict");
  }

  /**
   * Return a H2 specific sequence IdGenerator that supports batch fetching
   * sequence values.
   */
  @Override
  public IdGenerator createSequenceIdGenerator(BackgroundExecutor be, DataSource ds,
      String seqName, int batchSize) {

    return new H2SequenceIdGenerator(be, ds, seqName, batchSize);
  }

  @Override
  protected String withForUpdate(String sql) {
    return sql + " for update";
  }
}
