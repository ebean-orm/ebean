package io.ebeaninternal.server.deploy.parse;

import io.ebean.annotation.Aggregation;
import io.ebean.annotation.CreatedTimestamp;
import io.ebean.annotation.DbArray;
import io.ebean.annotation.DbComment;
import io.ebean.annotation.DbDefault;
import io.ebean.annotation.DbHstore;
import io.ebean.annotation.DbJson;
import io.ebean.annotation.DbJsonB;
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
import io.ebeaninternal.server.deploy.DbMigrationInfo;
import io.ebeaninternal.server.deploy.IndexDefinition;
import io.ebeaninternal.server.deploy.generatedproperty.GeneratedPropertyFactory;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssoc;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;
import io.ebeaninternal.server.type.DataEncryptSupport;
import io.ebeaninternal.server.type.ScalarType;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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

    Id id = get(prop, Id.class);
    if (id != null) {
      readIdAssocOne(prop);
    }

    EmbeddedId embeddedId = get(prop, EmbeddedId.class);
    if (embeddedId != null) {
      prop.setId();
      prop.setNullable(false);
      prop.setEmbedded();
    }

    DocEmbedded docEmbedded = get(prop, DocEmbedded.class);
    if (docEmbedded != null) {
      prop.setDocStoreEmbedded(docEmbedded.doc());
      if (descriptor.isDocStoreOnly()) {
        if (get(prop, ManyToOne.class) == null) {
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

    Formula formula = get(prop, Formula.class);
    if (formula != null) {
      prop.setSqlFormula(formula.select(), formula.join());
    }

    initWhoProperties(prop);
    readDbMigration(prop);
  }

  private void initWhoProperties(DeployBeanProperty prop) {
    if (get(prop, WhoModified.class) != null) {
      generatedPropFactory.setWhoModified(prop);
    }
    if (get(prop, WhoCreated.class) != null) {
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

    Column column = get(prop, Column.class);
    if (column != null) {
      readColumn(column, prop);
    }

    readJsonAnnotations(prop);

    if (prop.getDbColumn() == null) {
      // No @Column annotation or @Column.name() not set
      // Use the NamingConvention to set the DB column name
      String dbColumn = namingConvention.getColumnFromProperty(beanType, prop.getName());
      prop.setDbColumn(dbColumn);
    }

    GeneratedValue gen = get(prop, GeneratedValue.class);
    if (gen != null) {
      readGenValue(gen, prop);
    }

    Id id = get(prop, Id.class);
    if (id != null) {
      readIdScalar(prop);
    }

    // determine the JDBC type using Lob/Temporal
    // otherwise based on the property Class
    Temporal temporal = get(prop, Temporal.class);
    if (temporal != null) {
      readTemporal(temporal, prop);

    } else if (get(prop, Lob.class) != null) {
      util.setLobType(prop);
    }

    if (get(prop, TenantId.class) != null) {
      prop.setTenantId();
    }
    if (get(prop, Draft.class) != null) {
      prop.setDraft();
    }
    if (get(prop, DraftOnly.class) != null) {
      prop.setDraftOnly();
    }
    if (get(prop, DraftDirty.class) != null) {
      prop.setDraftDirty();
    }
    if (get(prop, DraftReset.class) != null) {
      prop.setDraftReset();
    }
    SoftDelete softDelete = get(prop, SoftDelete.class);
    if (softDelete != null) {
      prop.setSoftDelete();
    }

    DbComment comment = get(prop, DbComment.class);
    if (comment != null) {
      prop.setDbComment(comment.value());
    }
    DbHstore dbHstore = get(prop, DbHstore.class);
    if (dbHstore != null) {
      util.setDbHstore(prop, dbHstore);
    }
    DbJson dbJson = get(prop, DbJson.class);
    if (dbJson != null) {
      util.setDbJsonType(prop, dbJson);
    } else {
      DbJsonB dbJsonB = get(prop, DbJsonB.class);
      if (dbJsonB != null) {
        util.setDbJsonBType(prop, dbJsonB);
      }
    }
    DbArray dbArray = get(prop, DbArray.class);
    if (dbArray != null) {
      util.setDbArray(prop, dbArray);
    }

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

    Formula formula = get(prop, Formula.class);
    if (formula != null) {
      prop.setSqlFormula(formula.select(), formula.join());
    }

    Aggregation aggregation = get(prop, Aggregation.class);
    if (aggregation != null) {
      prop.setAggregation(aggregation.value());
    }

    Version version = get(prop, Version.class);
    if (version != null) {
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

    if (get(prop, WhenCreated.class) != null || get(prop, CreatedTimestamp.class) != null) {
      generatedPropFactory.setInsertTimestamp(prop);
    }

    if (get(prop, WhenModified.class) != null || get(prop, UpdatedTimestamp.class) != null) {
      generatedPropFactory.setUpdateTimestamp(prop);
    }

    initWhoProperties(prop);

    if (get(prop, HistoryExclude.class) != null) {
      prop.setExcludedFromHistory();
    }

    Length length = get(prop, Length.class);
    if (length != null) {
      prop.setDbLength(length.value());
    }

    io.ebean.annotation.NotNull nonNull = get(prop, io.ebean.annotation.NotNull.class);
    if (nonNull != null) {
      prop.setNullable(false);
    }

    readDbMigration(prop);


    if (validationAnnotations) {
      NotNull notNull = get(prop, NotNull.class);
      if (notNull != null && isEbeanValidationGroups(notNull.groups())) {
        // Not null on all validation groups so enable
        // DDL generation of Not Null Constraint
        prop.setNullable(false);
      }

      if (!prop.isLob()) {
        // take the max size of all @Size annotations
        int maxSize = -1;
        for (Size size : getAll(prop, Size.class)) {
          if (size.max() < Integer.MAX_VALUE) {
            maxSize = Math.max(maxSize, size.max());
          }
        }
        if (maxSize != -1) {
          prop.setDbLength(maxSize);
        }
      }
    }

    // Want to process last so we can use with @Formula
    Transient t = get(prop, Transient.class);
    if (t != null) {
      // it is not a persistent property.
      prop.setDbRead(false);
      prop.setDbInsertable(false);
      prop.setDbUpdateable(false);
      prop.setTransient();
    }

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

    Set<Index> indices = getAll(prop, Index.class);
    for (Index index : indices) {
      addIndex(prop, index);
    }
  }

  private void readDbMigration(DeployBeanProperty prop) {
    DbDefault dbDefault = get(prop, DbDefault.class);
    if (dbDefault != null) {
      prop.setDbColumnDefault(dbDefault.value());
    }

    Set<DbMigration> dbMigration = getAll(prop, DbMigration.class);
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
    descriptor.addIndex(new IndexDefinition(columnNames, index.name(), index.unique()));
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
    if (get(prop, UnmappedJson.class) != null) {
      prop.setUnmappedJson();
    }
  }

  private boolean hasRelationshipItem(DeployBeanProperty prop) {
    return get(prop, OneToMany.class) != null ||
      get(prop, ManyToOne.class) != null ||
      get(prop, OneToOne.class) != null;
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

  @SuppressWarnings({"unchecked", "rawtypes"})
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

  private void readGenValue(GeneratedValue gen, DeployBeanProperty prop) {

    descriptor.setIdGeneratedValue();
    String genName = gen.generator();

    SequenceGenerator sequenceGenerator = find(prop, SequenceGenerator.class);
    if (sequenceGenerator != null) {
      if (sequenceGenerator.name().equals(genName)) {
        genName = sequenceGenerator.sequenceName();
      }
      descriptor.setSequenceInitialValue(sequenceGenerator.initialValue());
      descriptor.setSequenceAllocationSize(sequenceGenerator.allocationSize());
    }

    GenerationType strategy = gen.strategy();

    if (strategy == GenerationType.IDENTITY) {
      descriptor.setIdType(IdType.IDENTITY);

    } else if (strategy == GenerationType.SEQUENCE) {
      descriptor.setIdType(IdType.SEQUENCE);
      if (!genName.isEmpty()) {
        descriptor.setIdGeneratorName(genName);
      }

    } else if (strategy == GenerationType.AUTO) {
      if (!genName.isEmpty()) {
        // use a custom IdGenerator
        PlatformIdGenerator idGenerator = generatedPropFactory.getIdGenerator(genName);
        if (idGenerator == null) {
          throw new IllegalStateException("No custom IdGenerator registered with name " + genName);
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
