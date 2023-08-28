package io.ebeaninternal.server.persist.dml;

import io.ebean.annotation.Platform;
import io.ebean.bean.EntityBean;
import io.ebean.config.dbplatform.DatabasePlatform;
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
  private final BindableId id;
  private final Bindable discriminator;
  private final BindableList all;
  private final boolean supportsGetGeneratedKeys;
  private final boolean concatenatedKey;
  /**
   * Used for DB that do not support getGeneratedKeys.
   */
  private final boolean supportsSelectLastInsertedId;
  private final Bindable shadowFKey;
  private final String[] identityDbColumns;
  private final Platform platform;

  InsertMeta(DatabasePlatform dbPlatform, BeanDescriptor<?> desc, Bindable shadowFKey, BindableId id, BindableList all) {
    this.platform = dbPlatform.platform();
    this.discriminator = discriminator(desc);
    this.id = id;
    this.all = all;
    this.shadowFKey = shadowFKey;

    String tableName = desc.baseTable();
    this.sqlWithId = genSql(false, tableName);

    // only available for single Id property
    if (id.isConcatenated()) {
      // concatenated key
      this.concatenatedKey = true;
      this.identityDbColumns = null;
      this.sqlNullId = null;
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
      this.sqlNullId = genSql(true, tableName);
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
  public void bind(DmlHandler request, EntityBean bean, boolean withId) throws SQLException {
    if (withId) {
      id.dmlBind(request, bean);
    }
    if (shadowFKey != null) {
      shadowFKey.dmlBind(request, bean);
    }
    if (discriminator != null) {
      discriminator.dmlBind(request, bean);
    }
    all.dmlBind(request, bean);
  }

  /**
   * get the sql based whether the id value(s) are null.
   */
  public String getSql(boolean withId) {
    if (withId) {
      return sqlWithId;
    } else {
      return sqlNullId;
    }
  }

  private String genSql(boolean nullId, String table) {
    GenerateDmlRequest request = new GenerateDmlRequest();
    request.setInsertSetMode();
    request.append("insert into ").append(table);
    if (nullId && noColumnsForInsert()) {
      return request.append(defaultValues()).toString();
    }
    request.append(" (");
    if (!nullId) {
      id.dmlAppend(request);
    }
    if (shadowFKey != null) {
      shadowFKey.dmlAppend(request);
    }
    if (discriminator != null) {
      discriminator.dmlAppend(request);
    }
    all.dmlAppend(request);

    request.append(") values (");
    request.append(request.insertBindBuffer());
    request.append(")");
    return request.toString();
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
  private boolean noColumnsForInsert() {
    return shadowFKey == null
      && discriminator == null
      && all.isEmpty();
  }

}
