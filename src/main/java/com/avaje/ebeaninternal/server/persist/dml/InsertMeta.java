package com.avaje.ebeaninternal.server.persist.dml;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.config.dbplatform.IdType;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.InheritInfo;
import com.avaje.ebeaninternal.server.persist.dmlbind.Bindable;
import com.avaje.ebeaninternal.server.persist.dmlbind.BindableDiscriminator;
import com.avaje.ebeaninternal.server.persist.dmlbind.BindableId;
import com.avaje.ebeaninternal.server.persist.dmlbind.BindableList;

import java.sql.SQLException;

/**
 * Meta data for insert handler. The meta data is for a particular bean type. It
 * is considered immutable and is thread safe.
 */
public final class InsertMeta {

  private enum IdGeneration {
    BY_SERVER, BY_CLIENT, BY_CLIENT_WITH_IDENTIY_INSERT;
  }
  private final String sqlNullId;
  private final String sqlWithId;
  private final String sqlDraftNullId;
  private final String sqlDraftWithId;

  private final BindableId id;

  private final Bindable discriminator;

  private final BindableList all;

  private final BindableList allExcludeDraftOnly;

  private final boolean supportsGetGeneratedKeys;

  private final boolean concatinatedKey;

  /**
   * Used for DB that do not support getGeneratedKeys.
   */
  private final String selectLastInsertedId;

  private final Bindable shadowFKey;

  private final String[] identityDbColumns;

  private final boolean emptyStringToNull;

  public InsertMeta(DatabasePlatform dbPlatform, BeanDescriptor<?> desc, Bindable shadowFKey, BindableId id, BindableList all) {

    this.emptyStringToNull = dbPlatform.isTreatEmptyStringsAsNull();
    this.discriminator = getDiscriminator(desc);
    this.id = id;
    this.all = all;
    this.allExcludeDraftOnly = all.excludeDraftOnly();
    this.shadowFKey = shadowFKey;

    String tableName = desc.getBaseTable();
    String draftTableName = desc.getDraftTable();
   
    // only available for single Id property
    if (id.isConcatenated()) {
      // concatenated key
      this.concatinatedKey = true;
      this.identityDbColumns = null;
      this.sqlNullId = null;
      this.sqlDraftNullId = null;
      this.supportsGetGeneratedKeys = false;
      this.selectLastInsertedId = null;

    } else {
      // insert sql for db identity or sequence insert
      this.concatinatedKey = false;
      if (id.getIdentityColumn() == null) {
        this.identityDbColumns = new String[]{};
        this.supportsGetGeneratedKeys = false;
        this.selectLastInsertedId = null;
      } else {
        this.identityDbColumns = new String[]{id.getIdentityColumn()};
        this.supportsGetGeneratedKeys = dbPlatform.getDbIdentity().isSupportsGetGeneratedKeys();
        this.selectLastInsertedId = desc.getSelectLastInsertedId();
      }
      this.sqlNullId = genSql(IdGeneration.BY_SERVER, tableName, false);
      this.sqlDraftNullId = desc.isDraftable() ? genSql(IdGeneration.BY_SERVER, draftTableName, true) : sqlNullId;
    }
    boolean idIsServerGenerated = (desc.getIdType() == IdType.IDENTITY || desc.getIdType() == IdType.SEQUENCE);
    if (idIsServerGenerated &&  desc.getIdProperty().isEmbedded()) {
      idIsServerGenerated = false; // AFAIK there is no support for embedded auto generated keys.
    }
    if (idIsServerGenerated && dbPlatform.needsIdentityInsert()) {
      this.sqlWithId = genSql(IdGeneration.BY_CLIENT_WITH_IDENTIY_INSERT, tableName, false);
      this.sqlDraftWithId = desc.isDraftable() ? genSql(IdGeneration.BY_CLIENT_WITH_IDENTIY_INSERT, draftTableName, true) : sqlWithId;
    } else {
      this.sqlWithId = genSql(IdGeneration.BY_CLIENT, tableName, false);
      this.sqlDraftWithId = desc.isDraftable() ? genSql(IdGeneration.BY_CLIENT, draftTableName, true) : sqlWithId;
    }
  }

  private static Bindable getDiscriminator(BeanDescriptor<?> desc) {
    InheritInfo inheritInfo = desc.getInheritInfo();
    if (inheritInfo != null) {
      return new BindableDiscriminator(inheritInfo);
    } else {
      return null;
    }
  }

  /**
   * Return true if empty strings should be treated as null.
   */
  public boolean isEmptyStringToNull() {
    return emptyStringToNull;
  }

  /**
   * Return true if this is a concatenated key.
   */
  public boolean isConcatenatedKey() {
    return concatinatedKey;
  }

  public String[] getIdentityDbColumns() {
    return identityDbColumns;
  }

  /**
   * Returns sql that is used to fetch back the last inserted id. This will
   * return null if it should not be used.
   * <p>
   * This is only for DB's that do not support getGeneratedKeys. For MS
   * SQLServer 2000 this could return "SELECT (at)(at)IDENTITY as id".
   * </p>
   */
  public String getSelectLastInsertedId() {
    return selectLastInsertedId;
  }

  /**
   * Return true if getGeneratedKeys is supported by the underlying jdbc
   * driver and database.
   */
  public boolean supportsGetGeneratedKeys() {
    return supportsGetGeneratedKeys;
  }

  /**
   * Return true if the Id can be derived from other property values.
   */
  public boolean deriveConcatenatedId(PersistRequestBean<?> persist) {
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
   * get the sql based whether the id value(s) are null.
   */
  public String getSql(boolean withId, boolean publish) {

    if (withId) {
      return publish ? sqlWithId : sqlDraftWithId;
    } else {
      return publish ? sqlNullId : sqlDraftNullId;
    }
  }

  private String genSql(IdGeneration idGeneration, String table, boolean draftTable) {

    GenerateDmlRequest request = new GenerateDmlRequest();
    request.setInsertSetMode();

    if (idGeneration == IdGeneration.BY_CLIENT_WITH_IDENTIY_INSERT) {
      // We must tell the Mssql-server that we excplicitly want to set an id and
      // override the auto generated id. (this seems to be a security/performance
      // feature, not to accidently set an id)
      request.append("SET IDENTITY_INSERT ").append(table).append(" ON;");
    }
    
    request.append("insert into ").append(table);
    request.append(" (");

    if (idGeneration == IdGeneration.BY_CLIENT || idGeneration == IdGeneration.BY_CLIENT_WITH_IDENTIY_INSERT) {
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

    request.append(") values (");
    request.append(request.getInsertBindBuffer());
    request.append(")");
    
    if (idGeneration == IdGeneration.BY_CLIENT_WITH_IDENTIY_INSERT) {
      request.append("; SET IDENTITY_INSERT ").append(table).append(" OFF");
    }

    return request.toString();
  }

}
