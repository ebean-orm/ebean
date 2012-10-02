package com.avaje.ebeaninternal.server.deploy.parse;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Types;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PersistenceException;
import javax.persistence.SequenceGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.EmbeddedColumns;
import com.avaje.ebean.annotation.Encrypted;
import com.avaje.ebean.annotation.Formula;
import com.avaje.ebean.annotation.LdapAttribute;
import com.avaje.ebean.annotation.LdapId;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.avaje.ebean.config.EncryptDeploy;
import com.avaje.ebean.config.EncryptDeploy.Mode;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.ebean.config.dbplatform.DbEncrypt;
import com.avaje.ebean.config.dbplatform.DbEncryptFunction;
import com.avaje.ebean.config.dbplatform.IdType;
import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotNull;
import com.avaje.ebean.validation.Pattern;
import com.avaje.ebean.validation.Patterns;
import com.avaje.ebean.validation.ValidatorMeta;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor.EntityType;
import com.avaje.ebeaninternal.server.deploy.generatedproperty.GeneratedPropertyFactory;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssoc;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertyCompound;
import com.avaje.ebeaninternal.server.idgen.UuidIdGenerator;
import com.avaje.ebeaninternal.server.lib.util.StringHelper;
import com.avaje.ebeaninternal.server.type.CtCompoundType;
import com.avaje.ebeaninternal.server.type.DataEncryptSupport;
import com.avaje.ebeaninternal.server.type.ScalarType;
import com.avaje.ebeaninternal.server.type.ScalarTypeBytesBase;
import com.avaje.ebeaninternal.server.type.ScalarTypeBytesEncrypted;
import com.avaje.ebeaninternal.server.type.ScalarTypeEncryptedWrapper;
import com.avaje.ebeaninternal.server.type.ScalarTypeLdapBoolean;
import com.avaje.ebeaninternal.server.type.ScalarTypeLdapDate;
import com.avaje.ebeaninternal.server.type.ScalarTypeLdapTimestamp;

/**
 * Read the field level deployment annotations.
 */
public class AnnotationFields extends AnnotationParser {

	/**
	 * By default we lazy load Lob properties.
	 */
	private FetchType defaultLobFetchType = FetchType.LAZY;
	
	private GeneratedPropertyFactory generatedPropFactory = new GeneratedPropertyFactory();

	public AnnotationFields(DeployBeanInfo<?> info) {
		super(info);
		
		if (GlobalProperties.getBoolean("ebean.lobEagerFetch", false)) {
			defaultLobFetchType = FetchType.EAGER;
		}
	}

	/**
	 * Read the field level deployment annotations.
	 */
	public void parse() {

		Iterator<DeployBeanProperty> it = descriptor.propertiesAll();
		while (it.hasNext()) {
			DeployBeanProperty prop = it.next();
			if (prop instanceof DeployBeanPropertyAssoc<?>) {
				readAssocOne(prop);
			} else {
				readField(prop);
			}

			readValidations(prop);
		}
	}

	/**
	 * Read the Id marker annotations on EmbeddedId properties.
	 */
	private void readAssocOne(DeployBeanProperty prop) {

		Id id = get(prop, Id.class);
		if (id != null) {
			prop.setId(true);
			prop.setNullable(false);
		}

		EmbeddedId embeddedId = get(prop, EmbeddedId.class);
		if (embeddedId != null) {
			prop.setId(true);
			prop.setNullable(false);
			prop.setEmbedded(true);
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
		LdapAttribute ldapAttribute = get(prop, LdapAttribute.class);
        if (ldapAttribute != null) {
            // read ldap specific property settings
            readLdapAttribute(ldapAttribute, prop);
        }
		
		if (prop.getDbColumn() == null){
		    if (EntityType.LDAP.equals(descriptor.getEntityType())) {
		        // just use matching for now. Could consider an LdapNamingConvention later.
		        prop.setDbColumn(prop.getName());
		    } else {
    			// No @Column annotation or @Column.name() not set
    			// Use the NamingConvention to set the DB column name
    			String dbColumn = namingConvention.getColumnFromProperty(beanType, prop.getName());
    			prop.setDbColumn(dbColumn);
		    }
		}

		GeneratedValue gen = get(prop, GeneratedValue.class);
		if (gen != null) {
			readGenValue(gen, prop);
		}

		Id id = (Id) get(prop, Id.class);
		if (id != null) {
			readId(id, prop);
		}
		LdapId ldapId = (LdapId)get(prop, LdapId.class);
        if (ldapId != null) {
            prop.setId(true);
            prop.setNullable(false);
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

		Formula formula = get(prop, Formula.class);
		if (formula != null) {
			prop.setSqlFormula(formula.select(), formula.join());
		}

		Version version = get(prop, Version.class);
		if (version != null) {
			// explicitly specify a version column
			prop.setVersionColumn(true);
			generatedPropFactory.setVersion(prop);
		}

		Basic basic = get(prop, Basic.class);
		if (basic != null) {
			prop.setFetchType(basic.fetch());
			if (!basic.optional()) {
				prop.setNullable(false);
			}
		} else if (prop.isLob()){
			// use the default Lob fetchType
			prop.setFetchType(defaultLobFetchType);
		}
		
		CreatedTimestamp ct = get(prop, CreatedTimestamp.class);
		if (ct != null) {
			generatedPropFactory.setInsertTimestamp(prop);
		}

		UpdatedTimestamp ut = get(prop, UpdatedTimestamp.class);
		if (ut != null) {
			generatedPropFactory.setUpdateTimestamp(prop);
		}

		NotNull notNull = get(prop, NotNull.class);
		if (notNull != null) {
			// explicitly specify a version column
			prop.setNullable(false);
		}

		Length length = get(prop, Length.class);
		if (length != null) {
			if (length.max() < Integer.MAX_VALUE){
				// explicitly specify a version column
				prop.setDbLength(length.max());
			}
		}
		
        EmbeddedColumns columns = get(prop, EmbeddedColumns.class);
        if (columns != null) {
            if (prop instanceof DeployBeanPropertyCompound){
                DeployBeanPropertyCompound p = (DeployBeanPropertyCompound)prop;

                // convert into a Map
                String propColumns = columns.columns();
                Map<String, String> propMap = StringHelper.delimitedToMap(propColumns, ",", "=");

                p.getDeployEmbedded().putAll(propMap);
                                
                CtCompoundType<?> compoundType = p.getCompoundType();
                if (compoundType == null){
                    throw new RuntimeException("No registered CtCompoundType for "+p.getPropertyType());
                }
                                
            } else {
                throw new RuntimeException("Can't use EmbeddedColumns on ScalarType "+prop.getFullBeanName());
            }
        }
		
		// Want to process last so we can use with @Formula 
		Transient t = get(prop, Transient.class);
		if (t != null) {
			// it is not a persistent property.
			prop.setDbRead(false);
			prop.setDbInsertable(false);
			prop.setDbUpdateable(false);
			prop.setTransient(true);
		}

		if (!prop.isTransient()){
		    
		    EncryptDeploy encryptDeploy = util.getEncryptDeploy(info.getDescriptor().getBaseTableFull(), prop.getDbColumn());
		    if (encryptDeploy == null || encryptDeploy.getMode().equals(Mode.MODE_ANNOTATION)){
	            Encrypted encrypted = get(prop, Encrypted.class);
	            if (encrypted != null) {    
	                setEncryption(prop, encrypted.dbEncryption(), encrypted.dbLength());
	            }		        
		    } else if (Mode.MODE_ENCRYPT.equals(encryptDeploy.getMode())) {
                setEncryption(prop, encryptDeploy.isDbEncrypt(), encryptDeploy.getDbLength());
		    }		        		    
		}
		
		if (EntityType.LDAP.equals(descriptor.getEntityType())){
		    adjustTypesForLdap(prop);
		}
	}
	
	private static final ScalarTypeLdapBoolean LDAP_BOOLEAN_SCALARTYPE = new ScalarTypeLdapBoolean();
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
    private void adjustTypesForLdap(DeployBeanProperty prop) {
	    
        Class<?> pt = prop.getPropertyType();
        if (boolean.class.equals(pt) || Boolean.class.equals(pt)){
            prop.setScalarType(LDAP_BOOLEAN_SCALARTYPE);
        
        } else {
            ScalarType<?> sqlScalarType = prop.getScalarType();
            int sqlType = sqlScalarType.getJdbcType();
            if (sqlType == Types.TIMESTAMP){
                // Use LDAP Timestamp String format
                prop.setScalarType(new ScalarTypeLdapTimestamp(sqlScalarType));
                
            } else if (sqlType == Types.DATE){
                // Use LDAP Timestamp String format
                prop.setScalarType(new ScalarTypeLdapDate(sqlScalarType));   
            
            } else {
                // Just using string parsing for all other types
            }
        }
	}
	
    private void setEncryption(DeployBeanProperty prop, boolean dbEncString, int dbLen) {
	    	    
        util.checkEncryptKeyManagerDefined(prop.getFullBeanName());
        
	    ScalarType<?> st = prop.getScalarType();
	    if (byte[].class.equals(st.getType())){
	        // Always using Java client encryption rather than DB for encryption
	        // of binary data (partially as this is not supported on all db's etc)
	        // This could be reviewed at a later stage.
	        ScalarTypeBytesBase baseType = (ScalarTypeBytesBase)st;
	        DataEncryptSupport support = createDataEncryptSupport(prop);
	        ScalarTypeBytesEncrypted encryptedScalarType = new ScalarTypeBytesEncrypted(baseType, support);
	        prop.setScalarType(encryptedScalarType);
	        prop.setLocalEncrypted(true);
	        return;
	    
	    } 
	    if (dbEncString){
	        
            DbEncrypt dbEncrypt = util.getDbPlatform().getDbEncrypt();
	        
	        if (dbEncrypt != null){
	            // check if we have a DB encryption function for this type
	            int jdbcType = prop.getScalarType().getJdbcType();
	            DbEncryptFunction dbEncryptFunction = dbEncrypt.getDbEncryptFunction(jdbcType);
	            if (dbEncryptFunction != null){
	                // Use DB functions to encrypt and decrypt
	                prop.setDbEncryptFunction(dbEncryptFunction, dbEncrypt, dbLen);
	                return;
	            }
	        }
	    }
	    
	    prop.setScalarType(createScalarType(prop, st));
	    prop.setLocalEncrypted(true);
        if (dbLen > 0){
            prop.setDbLength(dbLen);
        }
	}
	
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private ScalarTypeEncryptedWrapper<?> createScalarType(DeployBeanProperty prop, ScalarType<?> st ) {
        
        // Use Java Encryptor wrapping the logical scalar type 
        DataEncryptSupport support = createDataEncryptSupport(prop);
        ScalarTypeBytesBase byteType = getDbEncryptType(prop);
        
        return new ScalarTypeEncryptedWrapper(st, byteType, support);
	}
	
	private ScalarTypeBytesBase getDbEncryptType(DeployBeanProperty prop) {
	    int dbType = prop.isLob() ? Types.BLOB : Types.VARBINARY;
	    return (ScalarTypeBytesBase)util.getTypeManager().getScalarType(dbType);
	}
		
	private DataEncryptSupport createDataEncryptSupport(DeployBeanProperty prop) {
	    
	    String table = info.getDescriptor().getBaseTable();
	    String column = prop.getDbColumn();
	    
	    return util.createDataEncryptSupport(table, column);
	}
	

	private void readId(Id id, DeployBeanProperty prop) {

		prop.setId(true);
		prop.setNullable(false);

		if (prop.getPropertyType().equals(UUID.class)){
			// An Id of type UUID
			if (descriptor.getIdGeneratorName() == null){
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
			if (prop.getPropertyType().equals(UUID.class)){
				descriptor.setIdGeneratorName(UuidIdGenerator.AUTO_UUID);
				descriptor.setIdType(IdType.GENERATOR);

			} else {
				// use DatabasePlatform defaults
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

    
	private void readColumn(Column columnAnn, DeployBeanProperty prop) {

		if (!isEmpty(columnAnn.name())){
			String dbColumn = databasePlatform.convertQuotedIdentifiers(columnAnn.name());
			prop.setDbColumn(dbColumn);
		}

		prop.setDbInsertable(columnAnn.insertable());
		prop.setDbUpdateable(columnAnn.updatable());
		prop.setNullable(columnAnn.nullable());
		prop.setUnique(columnAnn.unique());
		if (columnAnn.precision() > 0){
			prop.setDbLength(columnAnn.precision());
		} else if (columnAnn.length() != 255){
			// set default 255 on DbTypeMap
			prop.setDbLength(columnAnn.length());
		}
		prop.setDbScale(columnAnn.scale());
		prop.setDbColumnDefn(columnAnn.columnDefinition());

		String baseTable = descriptor.getBaseTable();
		String tableName = columnAnn.table();
		if (tableName.equals("") || tableName.equalsIgnoreCase(baseTable)) {
			// its a base table property...
		} else {
			// its on a secondary table...
		    prop.setSecondaryTable(tableName);
			//DeployTableJoin tableJoin = info.getTableJoin(tableName);
			//tableJoin.addProperty(prop);
		}
	}



	private void readValidations(DeployBeanProperty prop) {

		Field field = prop.getField();
		if (field != null) {
			Annotation[] fieldAnnotations = field.getAnnotations();
			for (int i = 0; i < fieldAnnotations.length; i++) {
				readValidations(prop, fieldAnnotations[i]);
			}
		}

		Method readMethod = prop.getReadMethod();
		if (readMethod != null) {
			Annotation[] methAnnotations = readMethod.getAnnotations();
			for (int i = 0; i < methAnnotations.length; i++) {
				readValidations(prop, methAnnotations[i]);
			}
		}
	}

	private void readValidations(DeployBeanProperty prop, Annotation ann) {
		Class<?> type = ann.annotationType();
		if (type.equals(Patterns.class)){
			// treating this as a special case for now...
			Patterns patterns = (Patterns)ann;
			Pattern[] patternsArray = patterns.patterns();
			for (int i = 0; i < patternsArray.length; i++) {
				util.createValidator(prop, patternsArray[i]);
			}

		} else {

			ValidatorMeta meta = type.getAnnotation(ValidatorMeta.class);
			if (meta != null) {
				util.createValidator(prop, ann);
			}
		}
	}
}
