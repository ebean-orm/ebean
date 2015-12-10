package com.avaje.ebeaninternal.server.deploy.parse;

import com.avaje.ebean.annotation.*;
import com.avaje.ebean.config.EncryptDeploy;
import com.avaje.ebean.config.EncryptDeploy.Mode;
import com.avaje.ebean.config.dbplatform.DbEncrypt;
import com.avaje.ebean.config.dbplatform.DbEncryptFunction;
import com.avaje.ebean.config.dbplatform.IdType;
import com.avaje.ebeaninternal.server.deploy.generatedproperty.GeneratedPropertyFactory;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssoc;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertyCompound;
import com.avaje.ebeaninternal.server.idgen.UuidIdGenerator;
import com.avaje.ebeaninternal.server.lib.util.StringHelper;
import com.avaje.ebeaninternal.server.type.CtCompoundType;
import com.avaje.ebeaninternal.server.type.DataEncryptSupport;
import com.avaje.ebeaninternal.server.type.ScalarType;
import com.avaje.ebeaninternal.server.type.ScalarTypeBytesBase;
import com.avaje.ebeaninternal.server.type.ScalarTypeBytesEncrypted;
import com.avaje.ebeaninternal.server.type.ScalarTypeEncryptedWrapper;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Types;
import java.util.Map;
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

  public AnnotationFields(GeneratedPropertyFactory generatedPropFactory, DeployBeanInfo<?> info,
                          boolean javaxValidationAnnotations, boolean jacksonAnnotationsPresent, boolean eagerFetchLobs) {

    super(info, javaxValidationAnnotations);

    this.jacksonAnnotationsPresent = jacksonAnnotationsPresent;
    this.generatedPropFactory = generatedPropFactory;

    if (eagerFetchLobs) {
      defaultLobFetchType = FetchType.EAGER;
    }
  }

  /**
   * Read the field level deployment annotations.
   */
  public void parse() {

    for (DeployBeanProperty prop : descriptor.propertiesAll()) {
      if (prop instanceof DeployBeanPropertyAssoc<?>) {
        readAssocOne(prop);
      } else {
        readField(prop);
      }
    }
  }

  /**
   * Read the Id marker annotations on EmbeddedId properties.
   */
  private void readAssocOne(DeployBeanProperty prop) {

    readJsonAnnotations(prop);

    Id id = get(prop, Id.class);
    if (id != null) {
      prop.setId();
      prop.setNullable(false);
    }

    EmbeddedId embeddedId = get(prop, EmbeddedId.class);
    if (embeddedId != null) {
      prop.setId();
      prop.setNullable(false);
      prop.setEmbedded();
    }

    if (prop instanceof DeployBeanPropertyAssocOne<?>) {
      if (prop.isId() && !prop.isEmbedded()) {
        prop.setEmbedded();
      }
      readEmbeddedAttributeOverrides((DeployBeanPropertyAssocOne<?>) prop);
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
      readId(prop);
    }

    // determine the JDBC type using Lob/Temporal
    // otherwise based on the property Class
    Lob lob = get(prop, Lob.class);
    Temporal temporal = get(prop, Temporal.class);
    if (temporal != null) {
      readTemporal(temporal, prop);

    } else if (lob != null) {
      util.setLobType(prop);
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
    DbJson dbJson = get(prop, DbJson.class);
    if (dbJson != null) {
      util.setDbJsonType(prop, dbJson);
    } else {
      if (get(prop, DbJsonB.class) != null) {
        util.setDbJsonBType(prop);
      }
    }

    if (get(prop, DbHstore.class) != null) {
      util.setDbHstore(prop);
    }
    
    Formula formula = get(prop, Formula.class);
    if (formula != null) {
      prop.setSqlFormula(formula.select(), formula.join());
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

    if (get(prop, WhoCreated.class) != null) {
      generatedPropFactory.setWhoCreated(prop);
    }
    if (get(prop, WhoModified.class) != null) {
      generatedPropFactory.setWhoModified(prop);
    }

    if (get(prop, HistoryExclude.class) != null) {
      prop.setExcludedFromHistory();
    }

    if (validationAnnotations) {
      NotNull notNull = get(prop, NotNull.class);
      if (notNull != null && isNotNullOnAllValidationGroups(notNull.groups())) {
        // Not null on all validation groups so enable
        // DDL generation of Not Null Constraint
        prop.setNullable(false);
      }

      Size size = get(prop, Size.class);
      if (size != null) {
        if (size.max() < Integer.MAX_VALUE) {
          // explicitly specify a version column
          prop.setDbLength(size.max());
        }
      }
    }

    EmbeddedColumns columns = get(prop, EmbeddedColumns.class);
    if (columns != null) {
      if (prop instanceof DeployBeanPropertyCompound) {
        DeployBeanPropertyCompound p = (DeployBeanPropertyCompound) prop;

        // convert into a Map
        String propColumns = columns.columns();
        Map<String, String> propMap = StringHelper.delimitedToMap(propColumns, ",", "=");

        p.getDeployEmbedded().putAll(propMap);

        CtCompoundType<?> compoundType = p.getCompoundType();
        if (compoundType == null) {
          throw new RuntimeException("No registered CtCompoundType for " + p.getPropertyType());
        }

      } else {
        throw new RuntimeException("Can't use EmbeddedColumns on ScalarType " + prop.getFullBeanName());
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
      if (encryptDeploy == null || encryptDeploy.getMode().equals(Mode.MODE_ANNOTATION)) {
        Encrypted encrypted = get(prop, Encrypted.class);
        if (encrypted != null) {
          setEncryption(prop, encrypted.dbEncryption(), encrypted.dbLength());
        }
      } else if (Mode.MODE_ENCRYPT.equals(encryptDeploy.getMode())) {
        setEncryption(prop, encryptDeploy.isDbEncrypt(), encryptDeploy.getDbLength());
      }
    }

    Index index = get(prop, Index.class);
    if (index != null) {
      if (hasRelationshipItem(prop)) {
        throw new RuntimeException("Can't use Index on foreign key relationships.");
      }
      prop.setIndexed();
      prop.setIndexName(index.name());
    }
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
  }

  private boolean hasRelationshipItem(DeployBeanProperty prop) {
    return get(prop, OneToMany.class) != null ||
        get(prop, ManyToOne.class) != null ||
        get(prop, OneToOne.class) != null;
  }

  /**
   * Return true if the validation is on all validation groups and hence
   * can be applied to DDL generation.
   */
  private boolean isNotNullOnAllValidationGroups(Class<?>[] groups) {
    return groups.length == 0 || groups.length == 1 && javax.validation.groups.Default.class.isAssignableFrom(groups[0]);
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

  private void readId(DeployBeanProperty prop) {

    prop.setId();
    prop.setNullable(false);

    if (prop.getPropertyType().equals(UUID.class)) {
      // An Id of type UUID
      if (descriptor.getIdGeneratorName() == null) {
        // Without a generator explicitly specified
        // so will use the default one AUTO_UUID
        descriptor.setIdGeneratorName(UuidIdGenerator.AUTO_UUID);
        descriptor.setIdType(IdType.GENERATOR);
      }
    }
  }

  private void readGenValue(GeneratedValue gen, DeployBeanProperty prop) {

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
      if (genName != null && genName.length() > 0) {
        descriptor.setIdGeneratorName(genName);
      }

    } else if (strategy == GenerationType.AUTO) {
      if (prop.getPropertyType().equals(UUID.class)) {
        descriptor.setIdGeneratorName(UuidIdGenerator.AUTO_UUID);
        descriptor.setIdType(IdType.GENERATOR);
      }
    }
  }

  private void readTemporal(Temporal temporal, DeployBeanProperty prop) {

    TemporalType type = temporal.value();
    if (type.equals(TemporalType.DATE)) {
      prop.setDbType(Types.DATE);

    } else if (type.equals(TemporalType.TIMESTAMP)) {
      prop.setDbType(Types.TIMESTAMP);

    } else if (type.equals(TemporalType.TIME)) {
      prop.setDbType(Types.TIME);

    } else {
      throw new PersistenceException("Unhandled type " + type);
    }
  }


}
