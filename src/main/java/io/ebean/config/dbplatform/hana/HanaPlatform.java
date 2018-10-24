package io.ebean.config.dbplatform.hana;

import io.ebean.Query.ForUpdate;
import io.ebean.annotation.PersistBatch;
import io.ebean.annotation.Platform;
import io.ebean.config.PlatformConfig;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.DbType;
import io.ebean.config.dbplatform.IdType;
import io.ebean.config.dbplatform.SqlErrorCodes;

public class HanaPlatform extends DatabasePlatform {

  public HanaPlatform() {
    this.platform = Platform.HANA;
    this.sqlLimiter = new HanaSqlLimiter();
    this.persistBatchOnCascade = PersistBatch.NONE;
    this.supportsResultSetConcurrencyModeUpdatable = false;
    this.columnAliasPrefix = null;

    this.historySupport = new HanaHistorySupport();
    this.basicSqlLimiter = new HanaBasicSqlLimiter();

    this.likeClauseRaw = "like ?";
    this.maxConstraintNameLength = 127;
    this.maxTableNameLength = 127;

    this.dbDefaultValue.setNow("current_timestamp");

    this.exceptionTranslator = new SqlErrorCodes().addAcquireLock("131", "133", "146")
      .addDataIntegrity("130", "429", "461", "462").addDuplicateKey("144", "301", "349").build();

    this.dbIdentity.setIdType(IdType.IDENTITY);
    this.dbIdentity.setSelectLastInsertedIdTemplate("select current_identity_value() from sys.dummy");
    this.dbIdentity.setSupportsGetGeneratedKeys(false);
    this.dbIdentity.setSupportsIdentity(true);

    this.dbTypeMap.put(DbType.BIGINT, new DbPlatformType("bigint", false));
    this.dbTypeMap.put(DbType.BINARY, new DbPlatformType("varbinary", 255));
    this.dbTypeMap.put(DbType.BIT, new DbPlatformType("smallint", false));
    this.dbTypeMap.put(DbType.BLOB, new DbPlatformType("blob", false));
    this.dbTypeMap.put(DbType.CHAR, new DbPlatformType("nvarchar", 255));
    this.dbTypeMap.put(DbType.CLOB, new DbPlatformType("nclob", false));
    this.dbTypeMap.put(DbType.INTEGER, new DbPlatformType("integer", false));
    this.dbTypeMap.put(DbType.JSONVARCHAR, new DbPlatformType("nvarchar", 255));
    this.dbTypeMap.put(DbType.LINESTRING, new DbPlatformType("st_geometry"));
    this.dbTypeMap.put(DbType.LONGVARBINARY, new DbPlatformType("blob", false));
    this.dbTypeMap.put(DbType.LONGVARCHAR, new DbPlatformType("nclob", false));
    this.dbTypeMap.put(DbType.MULTILINESTRING, new DbPlatformType("st_geometry"));
    this.dbTypeMap.put(DbType.MULTIPOINT, new DbPlatformType("st_geometry"));
    this.dbTypeMap.put(DbType.MULTIPOLYGON, new DbPlatformType("st_geometry"));
    this.dbTypeMap.put(DbType.POINT, new DbPlatformType("st_point"));
    this.dbTypeMap.put(DbType.POLYGON, new DbPlatformType("st_geometry"));
    this.dbTypeMap.put(DbType.SMALLINT, new DbPlatformType("smallint", false));
    this.dbTypeMap.put(DbType.TINYINT, new DbPlatformType("smallint", false));
    this.dbTypeMap.put(DbType.UUID, new DbPlatformType("varchar", 40));
    this.dbTypeMap.put(DbType.VARBINARY, new DbPlatformType("varbinary", 255));
    this.dbTypeMap.put(DbType.VARCHAR, new DbPlatformType("nvarchar", 255));
  }

  @Override
  protected void addGeoTypes(int srid) {
    this.dbTypeMap.put(DbType.LINESTRING, new DbPlatformType("st_geometry(" + srid + ")", false));
    this.dbTypeMap.put(DbType.MULTILINESTRING, new DbPlatformType("st_geometry(" + srid + ")", false));
    this.dbTypeMap.put(DbType.MULTIPOINT, new DbPlatformType("st_geometry(" + srid + ")", false));
    this.dbTypeMap.put(DbType.MULTIPOLYGON, new DbPlatformType("st_geometry(" + srid + ")", false));
    this.dbTypeMap.put(DbType.POINT, new DbPlatformType("st_point(" + srid + ")", false));
    this.dbTypeMap.put(DbType.POLYGON, new DbPlatformType("st_geometry(" + srid + ")", false));
  }

  @Override
  protected String withForUpdate(String sql, ForUpdate forUpdateMode) {
    switch (forUpdateMode) {
      case BASE:
        return sql + " for update";
      case NOWAIT:
        return sql + " for update nowait";
      case SKIPLOCKED:
        return sql + " for update ignore locked";
      default:
        throw new IllegalArgumentException("Unknown update mode: " + forUpdateMode.name());
    }
  }

  @Override
  protected void configure(PlatformConfig config, boolean allQuotedIdentifiers) {
    super.configure(config, allQuotedIdentifiers);
    if (config.getDbUuid().useBinary()) {
      this.dbTypeMap.put(DbType.UUID, new DbPlatformType("varbinary", 16));
    } else {
      this.dbTypeMap.put(DbType.UUID, new DbPlatformType("varchar", 40));
    }
  }

}
