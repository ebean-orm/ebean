package io.ebeaninternal.server.persist.dml;

import io.ebean.InsertOptions;
import io.ebean.annotation.Platform;
import io.ebean.bean.EntityBean;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.InsertSqlSyntaxExtension;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.InheritInfo;
import io.ebeaninternal.server.persist.dmlbind.Bindable;
import io.ebeaninternal.server.persist.dmlbind.BindableDiscriminator;
import io.ebeaninternal.server.persist.dmlbind.BindableId;
import io.ebeaninternal.server.persist.dmlbind.BindableList;

import java.sql.SQLException;

/**
 * Metadata for insert handler. The metadata is for a particular bean type. It
 * is considered immutable and is thread safe.
 */
final class InsertMeta {

  private final String sqlNullId;
  private final String sqlWithId;
  private final String sqlDraftNullId;
  private final String sqlDraftWithId;
  private final BindableId id;
  private final Bindable discriminator;
  private final BindableList all;
  private final BindableList allExcludeDraftOnly;
  private final boolean supportsGetGeneratedKeys;
  private final boolean concatenatedKey;
  /**
   * Used for DB that do not support getGeneratedKeys.
   */
  private final boolean supportsSelectLastInsertedId;
  private final Bindable shadowFKey;
  private final String[] identityDbColumns;
  private final Platform platform;
  private final InsertSqlSyntaxExtension insertMetaSql;

  private final InsertMetaOptions options;

  InsertMeta(DatabasePlatform dbPlatform, BeanDescriptor<?> desc, Bindable shadowFKey, BindableId id, BindableList all) {
    this.platform = dbPlatform.platform();
    this.insertMetaSql = dbPlatform.insertSqlSyntaxExtension();
    this.options = InsertMetaPlatform.create(platform, desc, this);
    this.discriminator = discriminator(desc);
    this.id = id;
    this.all = all;
    this.allExcludeDraftOnly = all.excludeDraftOnly();
    this.shadowFKey = shadowFKey;

    String tableName = desc.baseTable();
    String draftTableName = desc.draftTable();
    this.sqlWithId = sql(false, tableName, false);
    this.sqlDraftWithId = desc.isDraftable() ? sql(false, draftTableName, true) : sqlWithId;

    // only available for single Id property
    if (id.isConcatenated()) {
      // concatenated key
      this.concatenatedKey = true;
      this.identityDbColumns = null;
      this.sqlNullId = null;
      this.sqlDraftNullId = null;
      this.supportsGetGeneratedKeys = false;
      this.supportsSelectLastInsertedId = false;

    } else {
      // insert sql for db identity or sequence insert
      this.concatenatedKey = false;
      if (id.getIdentityColumn() == null) {
        this.identityDbColumns = new String[]{};
        this.supportsGetGeneratedKeys = false;
        this.supportsSelectLastInsertedId = false;
      } else {
        this.identityDbColumns = new String[]{id.getIdentityColumn()};
        this.supportsGetGeneratedKeys = dbPlatform.dbIdentity().isSupportsGetGeneratedKeys();
        this.supportsSelectLastInsertedId = desc.supportsSelectLastInsertedId();
      }
      this.sqlNullId = sql(true, tableName, false);
      this.sqlDraftNullId = desc.isDraftable() ? sql(true, draftTableName, true) : sqlNullId;
    }
  }

  private static Bindable discriminator(BeanDescriptor<?> desc) {
    InheritInfo inheritInfo = desc.inheritInfo();
    return inheritInfo != null ? new BindableDiscriminator(inheritInfo) : null;
  }

  /**
   * Return true if this is a concatenated key.
   */
  boolean isConcatenatedKey() {
    return concatenatedKey;
  }

  String[] identityDbColumns() {
    return identityDbColumns;
  }

  /**
   * Return true if we should use a SQL query to return the generated key.
   * This can not be used with JDBC batch mode.
   */
  boolean supportsSelectLastInsertedId() {
    return supportsSelectLastInsertedId;
  }

  /**
   * Return true if getGeneratedKeys is supported by the underlying jdbc
   * driver and database.
   */
  boolean supportsGetGeneratedKeys() {
    return supportsGetGeneratedKeys;
  }

  /**
   * Return true if the Id can be derived from other property values.
   */
  boolean deriveConcatenatedId(PersistRequestBean<?> persist) {
    return id.deriveConcatenatedId(persist);
  }

  /**
   * Bind the request based on whether the id value(s) are null.
   */
  public void bind(DmlHandler request, EntityBean bean, boolean withId, boolean publish) throws SQLException {
    if (withId) {
      id.dmlBind(request, bean);
    }
    if (shadowFKey != null) {
      shadowFKey.dmlBind(request, bean);
    }
    if (discriminator != null) {
      discriminator.dmlBind(request, bean);
    }
    if (publish) {
      allExcludeDraftOnly.dmlBind(request, bean);
    } else {
      all.dmlBind(request, bean);
    }
  }

  /**
   * Return the sql for the given options.
   */
  public String sql(boolean withId, boolean publish, InsertOptions insertOptions) {
    if (insertOptions == null) {
      return sql(withId, publish);
    }
    return options.sql(withId, insertOptions);
  }

  String sqlFor(boolean withId) {
    return withId ? sqlWithId : sqlNullId;
  }

  private String sql(boolean withId, boolean publish) {
    if (withId) {
      return publish ? sqlWithId : sqlDraftWithId;
    } else {
      return publish ? sqlNullId : sqlDraftNullId;
    }
  }

  private String sql(boolean nullId, String table, boolean draftTable) {
    GenerateDmlRequest request = new GenerateDmlRequest();
    sql(request, nullId, table, draftTable);
    return request.toString();
  }

  void sql(GenerateDmlRequest request, boolean nullId, String table, boolean draftTable) {
    request.setInsertSetMode();
    request.append("insert into ").append(table);
    if (nullId && noColumnsForInsert(draftTable)) {
      request.append(defaultValues());
      return;
    }
    request.append(insertMetaSql.startColumns());
    if (!nullId) {
      id.dmlAppend(request);
    }
    if (shadowFKey != null) {
      shadowFKey.dmlAppend(request);
    }
    if (discriminator != null) {
      discriminator.dmlAppend(request);
    }
    if (draftTable) {
      all.dmlAppend(request);
    } else {
      allExcludeDraftOnly.dmlAppend(request);
    }
    request.append(insertMetaSql.endColumns());
    if (insertMetaSql.useBinding()) {
      request.append(request.insertBindBuffer());
      request.append(")");
    } else {
      request.append(insertMetaSql.startTypes());
      if (!nullId) {
        id.dmlType(request);
      }
      if (shadowFKey != null) {
        shadowFKey.dmlType(request);
      }
      if (discriminator != null) {
        discriminator.dmlType(request);
      }
      if (draftTable) {
        all.dmlType(request);
      } else {
        allExcludeDraftOnly.dmlType(request);
      }
      request.append(insertMetaSql.endTypes());
    }
  }

  private String defaultValues() {
    switch (platform.base()) {
      case MYSQL:
      case MARIADB:
      case ORACLE:
        return " values (default)";
      case DB2:
        return " (" + id.getIdentityColumn() + ") values (default)";
      default:
        return " default values";
    }
  }

  /**
   * Return true if the insert actually contains no columns.
   */
  private boolean noColumnsForInsert(boolean draftTable) {
    return shadowFKey == null
      && discriminator == null
      && (draftTable ? all.isEmpty() : allExcludeDraftOnly.isEmpty());
  }
}
