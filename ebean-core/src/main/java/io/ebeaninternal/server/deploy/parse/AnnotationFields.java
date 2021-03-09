package io.ebeaninternal.server.deploy.parse;

import io.ebean.annotation.Aggregation;
import io.ebean.annotation.CreatedTimestamp;
import io.ebean.annotation.DbArray;
import io.ebean.annotation.DbComment;
import io.ebean.annotation.DbDefault;
import io.ebean.annotation.DbJson;
import io.ebean.annotation.DbJsonB;
import io.ebean.annotation.DbMap;
import io.ebean.annotation.DbMigration;
import io.ebean.annotation.DocCode;
import io.ebean.annotation.DocEmbedded;
import io.ebean.annotation.DocProperty;
import io.ebean.annotation.DocSortable;
import io.ebean.annotation.Draft;
import io.ebean.annotation.DraftDirty;
import io.ebean.annotation.DraftOnly;
import io.ebean.annotation.DraftReset;
import io.ebean.annotation.Encrypted;
import io.ebean.annotation.Expose;
import io.ebean.annotation.Formula;
import io.ebean.annotation.HistoryExclude;
import io.ebean.annotation.Identity;
import io.ebean.annotation.Index;
import io.ebean.annotation.JsonIgnore;
import io.ebean.annotation.Length;
import io.ebean.annotation.SoftDelete;
import io.ebean.annotation.TenantId;
import io.ebean.annotation.UnmappedJson;
import io.ebean.annotation.UpdatedTimestamp;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import io.ebean.annotation.WhoCreated;
import io.ebean.annotation.WhoModified;
import io.ebean.config.EncryptDeploy;
import io.ebean.config.EncryptDeploy.Mode;
import io.ebean.config.dbplatform.DbEncrypt;
import io.ebean.config.dbplatform.DbEncryptFunction;
import io.ebean.config.dbplatform.IdType;
import io.ebean.config.dbplatform.PlatformIdGenerator;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.server.deploy.DbMigrationInfo;
import io.ebeaninternal.server.deploy.IndexDefinition;
import io.ebeaninternal.server.deploy.generatedproperty.GeneratedPropertyFactory;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssoc;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;
import io.ebeaninternal.server.type.DataEncryptSupport;
import io.ebeaninternal.server.type.ScalarTypeBytesBase;
import io.ebeaninternal.server.type.ScalarTypeBytesEncrypted;
import io.ebeaninternal.server.type.ScalarTypeEncryptedWrapper;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceException;
import javax.persistence.SequenceGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.sql.Types;
import java.util.Set;
import java.util.UUID;

/**
 * Read the field level deployment annotations.
 */
public class AnnotationFields extends AnnotationParser {

  /**
   * If present read Jackson JsonIgnore.
   */
  private final boolean jacksonAnnotationsPresent;

  private final GeneratedPropertyFactory generatedPropFactory;

  /**
   * By default we lazy load Lob properties.
   */
  private FetchType defaultLobFetchType = FetchType.LAZY;

  AnnotationFields(DeployBeanInfo<?> info, ReadAnnotationConfig readConfig) {
    super(info, readConfig);
    this.jacksonAnnotationsPresent = readConfig.isJacksonAnnotations();
    this.generatedPropFactory = readConfig.getGeneratedPropFactory();
    if (readConfig.isEagerFetchLobs()) {
      defaultLobFetchType = FetchType.EAGER;
    }
  }

  /**
   * Read the field level deployment annotations.
   */
  @Override
  public void parse() {
    for (DeployBeanProperty prop : descriptor.propertiesAll()) {
      prop.initMetaAnnotations(readConfig.getMetaAnnotations());
      if (prop instanceof DeployBeanPropertyAssoc<?>) {
        readAssocOne((DeployBeanPropertyAssoc<?>) prop);
      } else {
        readField(prop);
      }
    }
  }

  /**
   * Read the Id marker annotations on EmbeddedId properties.
   */
  private void readAssocOne(DeployBeanPropertyAssoc<?> prop) {

    readJsonAnnotations(prop);

    if (has(prop, Id.class)) {
      readIdAssocOne(prop);
    }

    if (has(prop, EmbeddedId.class)) {
      prop.setId();
      prop.setNullable(false);
      prop.setEmbedded();
      info.setEmbeddedId(prop);
    }

    DocEmbedded docEmbedded = get(prop, DocEmbedded.class);
    if (docEmbedded != null) {
      prop.setDocStoreEmbedded(docEmbedded.doc());
      if (descriptor.isDocStoreOnly()) {
        if (has(prop, ManyToOne.class)) {
          prop.setEmbedded();
          prop.setDbInsertable(true);
          prop.setDbUpdateable(true);
        }
      }
    }

    if (prop instanceof DeployBeanPropertyAssocOne<?>) {
      if (prop.isId() && !prop.isEmbedded()) {
        prop.setEmbedded();
      }
      readEmbeddedAttributeOverrides((DeployBeanPropertyAssocOne<?>) prop);
    }

    Formula formula = prop.getMetaAnnotationFormula(platform);
    if (formula != null) {
      prop.setSqlFormula(formula.select(), formula.join());
    }

    initWhoProperties(prop);
    initDbMigration(prop);
  }

  private void initWhoProperties(DeployBeanProperty prop) {
    if (has(prop, WhoModified.class)) {
      generatedPropFactory.setWhoModified(prop);
    }
    if (has(prop, WhoCreated.class)) {
      generatedPropFactory.setWhoCreated(prop);
    }
  }

  private void readField(DeployBeanProperty prop) {

    // all Enums will have a ScalarType assigned...
    boolean isEnum = prop.getPropertyType().isEnum();
    Enumerated enumerated = get(prop, Enumerated.class);
    if (isEnum || enumerated != null) {
      util.setEnumScalarType(enumerated, prop);
    }

    // its persistent and assumed to be on the base table
    // rather than on a secondary table
    prop.setDbRead(true);
    prop.setDbInsertable(true);
    prop.setDbUpdateable(true);

    Column column = prop.getMetaAnnotation(Column.class);
    if (column != null) {
      readColumn(column, prop);
    }

    readJsonAnnotations(prop);

    if (prop.getDbColumn() == null) {
      // No @Column or @Column.name() so use NamingConvention
      prop.setDbColumn(namingConvention.getColumnFromProperty(beanType, prop.getName()));
    }

    initIdentity(prop);
    initTenantId(prop);
    initDbJson(prop);
    initFormula(prop);
    initVersion(prop);
    initWhen(prop);
    initWhoProperties(prop);
    initDbMigration(prop);

    // Want to process last so we can use with @Formula
    if (has(prop, Transient.class)) {
      // it is not a persistent property.
      prop.setDbRead(false);
      prop.setDbInsertable(false);
      prop.setDbUpdateable(false);
      prop.setTransient();
    }

    initEncrypt(prop);

    for (Index index : annotationIndexes(prop)) {
      addIndex(prop, index);
    }
  }

  private void initIdentity(DeployBeanProperty prop) {
    Id id = get(prop, Id.class);
    GeneratedValue gen = get(prop, GeneratedValue.class);
    if (gen != null) {
      readGenValue(gen, id, prop);
    }
    if (id != null) {
      readIdScalar(prop);
    }
    Identity identity = get(prop, Identity.class);
    if (identity != null) {
      readIdentity(identity);
    }

    // determine the JDBC type using Lob/Temporal
    // otherwise based on the property Class
    Temporal temporal = get(prop, Temporal.class);
    if (temporal != null) {
      readTemporal(temporal, prop);

    } else if (has(prop, Lob.class)) {
      util.setLobType(prop);
    }

    Length length = get(prop, Length.class);
    if (length != null) {
      prop.setDbLength(length.value());
    }

    if (has(prop, io.ebean.annotation.NotNull.class)) {
      prop.setNullable(false);
    }
  }

  private void initValidation(DeployBeanProperty prop) {
    if (readConfig.isValidationNotNull(prop)) {
      prop.setNullable(false);
    }
    if (!prop.isLob()) {
      int maxSize = readConfig.maxValidationSize(prop);
      if (maxSize > 0) {
        prop.setDbLength(maxSize);
      }
    }
  }

  private void initTenantId(DeployBeanProperty prop) {
    if (readConfig.checkValidationAnnotations()) {
      initValidation(prop);
    }
    if (has(prop, TenantId.class)) {
      prop.setTenantId();
    }
    if (has(prop, Draft.class)) {
      prop.setDraft();
    }
    if (has(prop, DraftOnly.class)) {
      prop.setDraftOnly();
    }
    if (has(prop, DraftDirty.class)) {
      prop.setDraftDirty();
    }
    if (has(prop, DraftReset.class)) {
      prop.setDraftReset();
    }
    if (has(prop, SoftDelete.class)) {
      prop.setSoftDelete();
    }
  }

  private void initDbJson(DeployBeanProperty prop) {
    DbComment comment = get(prop, DbComment.class);
    if (comment != null) {
      prop.setDbComment(comment.value());
    }
    DbMap dbMap = get(prop, DbMap.class);
    if (dbMap != null) {
      util.setDbMap(prop, dbMap);
      setColumnName(prop, dbMap.name());
    }
    DbJson dbJson = get(prop, DbJson.class);
    if (dbJson != null) {
      util.setDbJsonType(prop, dbJson);
      setColumnName(prop, dbJson.name());
    } else {
      DbJsonB dbJsonB = get(prop, DbJsonB.class);
      if (dbJsonB != null) {
        util.setDbJsonBType(prop, dbJsonB);
        setColumnName(prop, dbJsonB.name());
      }
    }
    DbArray dbArray = get(prop, DbArray.class);
    if (dbArray != null) {
      util.setDbArray(prop, dbArray);
      setColumnName(prop, dbArray.name());
    }
  }

  private void initFormula(DeployBeanProperty prop) {
    DocCode docCode = get(prop, DocCode.class);
    if (docCode != null) {
      prop.setDocCode(docCode);
    }
    DocSortable docSortable = get(prop, DocSortable.class);
    if (docSortable != null) {
      prop.setDocSortable(docSortable);
    }
    DocProperty docProperty = get(prop, DocProperty.class);
    if (docProperty != null) {
      prop.setDocProperty(docProperty);
    }
    Formula formula = prop.getMetaAnnotationFormula(platform);
    if (formula != null) {
      prop.setSqlFormula(formula.select(), formula.join());
    }

    final Aggregation aggregation = prop.getMetaAnnotation(Aggregation.class);
    if (aggregation != null) {
      prop.setAggregation(aggregation.value().replace("$1", prop.getName()));
    }
  }

  private void initVersion(DeployBeanProperty prop) {
    if (has(prop, Version.class)) {
      // explicitly specify a version column
      prop.setVersionColumn();
      generatedPropFactory.setVersion(prop);
    }

    Basic basic = get(prop, Basic.class);
    if (basic != null) {
      prop.setFetchType(basic.fetch());
      if (!basic.optional()) {
        prop.setNullable(false);
      }
    } else if (prop.isLob()) {
      // use the default Lob fetchType
      prop.setFetchType(defaultLobFetchType);
    }
  }

  private void initWhen(DeployBeanProperty prop) {
    if (has(prop, WhenCreated.class) || has(prop, CreatedTimestamp.class)) {
      generatedPropFactory.setInsertTimestamp(prop);
    }
    if (has(prop, WhenModified.class) || has(prop, UpdatedTimestamp.class)) {
      generatedPropFactory.setUpdateTimestamp(prop);
    }
  }

  private void initEncrypt(DeployBeanProperty prop) {
    if (!prop.isTransient()) {
      EncryptDeploy encryptDeploy = util.getEncryptDeploy(info.getDescriptor().getBaseTableFull(), prop.getDbColumn());
      if (encryptDeploy == null || encryptDeploy.getMode() == Mode.MODE_ANNOTATION) {
        Encrypted encrypted = get(prop, Encrypted.class);
        if (encrypted != null) {
          setEncryption(prop, encrypted.dbEncryption(), encrypted.dbLength());
        }
      } else if (Mode.MODE_ENCRYPT == encryptDeploy.getMode()) {
        setEncryption(prop, encryptDeploy.isDbEncrypt(), encryptDeploy.getDbLength());
      }
    }
  }

  private void readIdentity(Identity identity) {
    descriptor.setIdentityMode(identity);
  }

  private void initDbMigration(DeployBeanProperty prop) {
    if (has(prop, HistoryExclude.class)) {
      prop.setExcludedFromHistory();
    }
    DbDefault dbDefault = get(prop, DbDefault.class);
    if (dbDefault != null) {
      prop.setDbColumnDefault(dbDefault.value());
    }

    Set<DbMigration> dbMigration = annotationDbMigrations(prop);
    dbMigration.forEach(ann -> prop.addDbMigrationInfo(
      new DbMigrationInfo(ann.preAdd(), ann.postAdd(), ann.preAlter(), ann.postAlter(), ann.platforms())));
  }

  private void addIndex(DeployBeanProperty prop, Index index) {
    String[] columnNames;
    if (index.columnNames().length == 0) {
      columnNames = new String[]{prop.getDbColumn()};
    } else {
      columnNames = new String[index.columnNames().length];
      int i = 0;
      int found = 0;
      for (String colName : index.columnNames()) {
        if (colName.equals("${fa}") || colName.equals(prop.getDbColumn())) {
          columnNames[i++] = prop.getDbColumn();
          found++;
        } else {
          columnNames[i++] = colName;
        }
      }
      if (found != 1) {
        throw new RuntimeException("DB-columname has to be specified exactly one time in columnNames.");
      }
    }

    if (columnNames.length == 1 && hasRelationshipItem(prop)) {
      throw new RuntimeException("Can't use Index on foreign key relationships.");
    }
    descriptor.addIndex(new IndexDefinition(columnNames, index.name(), index.unique(), index.platforms(), index.concurrent(), index.definition()));
  }

  private void readJsonAnnotations(DeployBeanProperty prop) {
    if (jacksonAnnotationsPresent) {
      com.fasterxml.jackson.annotation.JsonIgnore jsonIgnore = get(prop, com.fasterxml.jackson.annotation.JsonIgnore.class);
      if (jsonIgnore != null) {
        prop.setJsonSerialize(!jsonIgnore.value());
        prop.setJsonDeserialize(!jsonIgnore.value());
      }
    }

    Expose expose = get(prop, Expose.class);
    if (expose != null) {
      prop.setJsonSerialize(expose.serialize());
      prop.setJsonDeserialize(expose.deserialize());
    }

    JsonIgnore jsonIgnore = get(prop, JsonIgnore.class);
    if (jsonIgnore != null) {
      prop.setJsonSerialize(jsonIgnore.serialize());
      prop.setJsonDeserialize(jsonIgnore.deserialize());
    }
    if (has(prop, UnmappedJson.class)) {
      prop.setUnmappedJson();
    }
  }

  private boolean hasRelationshipItem(DeployBeanProperty prop) {
    return has(prop, OneToMany.class) || has(prop, ManyToOne.class) || has(prop, OneToOne.class);
  }

  private void setEncryption(DeployBeanProperty prop, boolean dbEncString, int dbLen) {

    util.checkEncryptKeyManagerDefined(prop.getFullBeanName());

    ScalarType<?> st = prop.getScalarType();
    if (byte[].class.equals(st.getType())) {
      // Always using Java client encryption rather than DB for encryption
      // of binary data (partially as this is not supported on all db's etc)
      // This could be reviewed at a later stage.
      ScalarTypeBytesBase baseType = (ScalarTypeBytesBase) st;
      DataEncryptSupport support = createDataEncryptSupport(prop);
      ScalarTypeBytesEncrypted encryptedScalarType = new ScalarTypeBytesEncrypted(baseType, support);
      prop.setScalarType(encryptedScalarType);
      prop.setLocalEncrypted();
      return;

    }
    if (dbEncString) {

      DbEncrypt dbEncrypt = util.getDbPlatform().getDbEncrypt();

      if (dbEncrypt != null) {
        // check if we have a DB encryption function for this type
        int jdbcType = prop.getScalarType().getJdbcType();
        DbEncryptFunction dbEncryptFunction = dbEncrypt.getDbEncryptFunction(jdbcType);
        if (dbEncryptFunction != null) {
          // Use DB functions to encrypt and decrypt
          prop.setDbEncryptFunction(dbEncryptFunction, dbEncrypt, dbLen);
          return;
        }
      }
    }

    prop.setScalarType(createScalarType(prop, st));
    prop.setLocalEncrypted();
    if (dbLen > 0) {
      prop.setDbLength(dbLen);
    }
  }

  @SuppressWarnings({"unchecked"})
  private ScalarTypeEncryptedWrapper<?> createScalarType(DeployBeanProperty prop, ScalarType<?> st) {

    // Use Java Encryptor wrapping the logical scalar type
    DataEncryptSupport support = createDataEncryptSupport(prop);
    ScalarTypeBytesBase byteType = getDbEncryptType(prop);

    return new ScalarTypeEncryptedWrapper(st, byteType, support);
  }

  private ScalarTypeBytesBase getDbEncryptType(DeployBeanProperty prop) {
    int dbType = prop.isLob() ? Types.BLOB : Types.VARBINARY;
    return (ScalarTypeBytesBase) util.getTypeManager().getScalarType(dbType);
  }

  private DataEncryptSupport createDataEncryptSupport(DeployBeanProperty prop) {

    String table = info.getDescriptor().getBaseTable();
    String column = prop.getDbColumn();

    return util.createDataEncryptSupport(table, column);
  }

  private void readGenValue(GeneratedValue gen, Id id, DeployBeanProperty prop) {
    if (id == null) {
      if (UUID.class.equals(prop.getPropertyType())) {
        generatedPropFactory.setUuid(prop);
        return;
      }
    }
    descriptor.setIdGeneratedValue();

    SequenceGenerator seq = get(prop, SequenceGenerator.class);
    if (seq != null) {
      String seqName = seq.sequenceName();
      if (seqName.isEmpty()) {
        seqName = namingConvention.getSequenceName(descriptor.getBaseTable(), prop.getDbColumn());
      }
      descriptor.setIdentitySequence(seq.initialValue(), seq.allocationSize(), seqName);
    }

    GenerationType strategy = gen.strategy();
    if (strategy == GenerationType.IDENTITY) {
      descriptor.setIdentityType(IdType.IDENTITY);

    } else if (strategy == GenerationType.SEQUENCE) {
      descriptor.setIdentityType(IdType.SEQUENCE);
      if (!gen.generator().isEmpty()) {
        descriptor.setIdentitySequenceGenerator(gen.generator());
      }

    } else if (strategy == GenerationType.AUTO) {
      if (!gen.generator().isEmpty()) {
        // use a custom IdGenerator
        PlatformIdGenerator idGenerator = generatedPropFactory.getIdGenerator(gen.generator());
        if (idGenerator == null) {
          throw new IllegalStateException("No custom IdGenerator registered with name " + gen.generator());
        }
        descriptor.setCustomIdGenerator(idGenerator);
      } else if (prop.getPropertyType().equals(UUID.class)) {
        descriptor.setUuidGenerator();
      }
    }
  }

  private void readTemporal(Temporal temporal, DeployBeanProperty prop) {

    TemporalType type = temporal.value();
    if (type == TemporalType.DATE) {
      prop.setDbType(Types.DATE);

    } else if (type == TemporalType.TIMESTAMP) {
      prop.setDbType(Types.TIMESTAMP);

    } else if (type == TemporalType.TIME) {
      prop.setDbType(Types.TIME);

    } else {
      throw new PersistenceException("Unhandled type " + type);
    }
  }


}
