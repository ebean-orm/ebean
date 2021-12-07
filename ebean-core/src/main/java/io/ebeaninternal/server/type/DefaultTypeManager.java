package io.ebeaninternal.server.type;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ebean.annotation.*;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.JsonConfig;
import io.ebean.config.PlatformConfig;
import io.ebean.config.ScalarTypeConverter;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.core.type.DocPropertyType;
import io.ebean.core.type.ExtraTypeFactory;
import io.ebean.core.type.ScalarType;
import io.ebean.types.Cidr;
import io.ebean.types.Inet;
import io.ebean.util.AnnotationUtil;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.api.DbOffline;
import io.ebeaninternal.api.GeoTypeProvider;
import io.ebeaninternal.server.core.ServiceUtil;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.slf4j.Logger;

import javax.persistence.AttributeConverter;
import javax.persistence.EnumType;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of TypeManager.
 * <p>
 * Manages the list of ScalarType that is available.
 */
public final class DefaultTypeManager implements TypeManager {

  private static final Logger log = CoreLog.internal;

  private final ConcurrentHashMap<Class<?>, ScalarType<?>> typeMap;
  private final ConcurrentHashMap<Integer, ScalarType<?>> nativeMap;
  private final ConcurrentHashMap<String, ScalarType<?>> logicalMap;

  private final DefaultTypeFactory extraTypeFactory;

  private final ScalarType<?> hstoreType = new ScalarTypePostgresHstore();
  private final ScalarTypeFile fileType = new ScalarTypeFile();
  private final ScalarType<?> charType = new ScalarTypeChar();
  private final ScalarType<?> charArrayType = new ScalarTypeCharArray();
  private final ScalarType<?> longVarcharType = new ScalarTypeLongVarchar();
  private final ScalarType<?> clobType = new ScalarTypeClob();
  private final ScalarType<?> byteType = new ScalarTypeByte();
  private final ScalarType<?> binaryType = new ScalarTypeBytesBinary();
  private final ScalarType<?> blobType = new ScalarTypeBytesBlob();
  private final ScalarType<?> varbinaryType = new ScalarTypeBytesVarbinary();
  private final ScalarType<?> longVarbinaryType = new ScalarTypeBytesLongVarbinary();
  private final ScalarType<?> shortType = new ScalarTypeShort();
  private final ScalarType<?> integerType = new ScalarTypeInteger();
  private final ScalarType<?> longType = new ScalarTypeLong();
  private final ScalarType<?> doubleType = new ScalarTypeDouble();
  private final ScalarType<?> floatType = new ScalarTypeFloat();
  private final ScalarType<?> bigDecimalType = new ScalarTypeBigDecimal();
  private final ScalarType<?> timeType = new ScalarTypeTime();
  private final ScalarType<?> urlType = new ScalarTypeURL();
  private final ScalarType<?> uriType = new ScalarTypeURI();
  private final ScalarType<?> localeType = new ScalarTypeLocale();
  private final ScalarType<?> currencyType = new ScalarTypeCurrency();
  private final ScalarType<?> timeZoneType = new ScalarTypeTimeZone();
  private final ScalarType<?> stringType = ScalarTypeString.INSTANCE;
  private final ScalarType<?> classType = new ScalarTypeClass();

  private final JsonConfig.DateTime jsonDateTime;
  private final JsonConfig.Date jsonDate;

  private final Object objectMapper;
  private final boolean objectMapperPresent;
  private final boolean postgres;
  private final TypeJsonManager jsonManager;
  private final boolean offlineMigrationGeneration;
  private final EnumType defaultEnumType;

  // OPTIONAL ScalarTypes registered if Jackson/JsonNode is in the classpath

  /**
   * Jackson's JsonNode storage to Clob.
   */
  private ScalarType<?> jsonNodeClob;
  /**
   * Jackson's JsonNode storage to Blob.
   */
  private ScalarType<?> jsonNodeBlob;
  /**
   * Jackson's JsonNode storage to Varchar.
   */
  private ScalarType<?> jsonNodeVarchar;
  /**
   * Jackson's JsonNode storage to Postgres JSON or Clob.
   */
  private ScalarType<?> jsonNodeJson;
  /**
   * Jackson's JsonNode storage to Postgres JSONB or Clob.
   */
  private ScalarType<?> jsonNodeJsonb;

  private final PlatformArrayTypeFactory arrayTypeListFactory;
  private final PlatformArrayTypeFactory arrayTypeSetFactory;
  private GeoTypeBinder geoTypeBinder;

  /**
   * Create the DefaultTypeManager.
   */
  public DefaultTypeManager(DatabaseConfig config, BootupClasses bootupClasses) {
    this.jsonDateTime = config.getJsonDateTime();
    this.jsonDate = config.getJsonDate();
    this.typeMap = new ConcurrentHashMap<>();
    this.nativeMap = new ConcurrentHashMap<>();
    this.logicalMap = new ConcurrentHashMap<>();
    this.postgres = isPostgres(config.getDatabasePlatform());
    this.objectMapperPresent = config.getClassLoadConfig().isJacksonObjectMapperPresent();
    this.objectMapper = (objectMapperPresent) ? initObjectMapper(config) : null;
    this.jsonManager = (objectMapperPresent) ? new TypeJsonManager(postgres, objectMapper, config.getJsonMutationDetection()) : null;
    this.extraTypeFactory = new DefaultTypeFactory(config);
    this.arrayTypeListFactory = arrayTypeListFactory(config.getDatabasePlatform());
    this.arrayTypeSetFactory = arrayTypeSetFactory(config.getDatabasePlatform());
    this.offlineMigrationGeneration = DbOffline.isGenerateMigration();
    this.defaultEnumType = config.getDefaultEnumType();

    initialiseStandard(config);
    initialiseJavaTimeTypes(config);
    initialiseJodaTypes(config);
    initialiseJacksonTypes(config);
    loadTypesFromProviders(config, objectMapper);
    loadGeoTypeBinder(config);

    if (bootupClasses != null) {
      initialiseCustomScalarTypes(bootupClasses);
      initialiseScalarConverters(bootupClasses);
      initialiseAttributeConverters(bootupClasses);
    }
  }

  private void loadGeoTypeBinder(DatabaseConfig config) {
    final GeoTypeProvider provider = ServiceUtil.service(GeoTypeProvider.class);
    if (provider != null) {
      geoTypeBinder = provider.createBinder(config);
    }
  }

  private PlatformArrayTypeFactory arrayTypeListFactory(DatabasePlatform databasePlatform) {
    if (databasePlatform.isNativeArrayType()) {
      return ScalarTypeArrayList.factory();
    } else if (databasePlatform.isPlatform(Platform.H2)) {
      return ScalarTypeArrayListH2.factory();
    }
    return new PlatformArrayTypeJsonList();
  }

  private PlatformArrayTypeFactory arrayTypeSetFactory(DatabasePlatform databasePlatform) {
    if (databasePlatform.isNativeArrayType()) {
      return ScalarTypeArraySet.factory();
    } else if (databasePlatform.isPlatform(Platform.H2)) {
      return ScalarTypeArraySetH2.factory();
    }
    return new PlatformArrayTypeJsonSet();
  }

  /**
   * Load custom scalar types registered via ExtraTypeFactory and ServiceLoader.
   */
  private void loadTypesFromProviders(DatabaseConfig config, Object objectMapper) {
    ServiceLoader<ExtraTypeFactory> factories = ServiceLoader.load(ExtraTypeFactory.class);
    Iterator<ExtraTypeFactory> iterator = factories.iterator();
    if (iterator.hasNext()) {
      // use the cacheFactory (via classpath service loader)
      ExtraTypeFactory plugin = iterator.next();
      List<? extends ScalarType<?>> types = plugin.createTypes(config, objectMapper);
      for (ScalarType<?> type : types) {
        log.debug("adding ScalarType {}", type.getClass());
        addCustomType(type);
      }
    }
  }

  private boolean isPostgres(DatabasePlatform databasePlatform) {
    return databasePlatform.getPlatform().base() == Platform.POSTGRES;
  }

  /**
   * Register a custom ScalarType.
   */
  @Override
  public void add(ScalarType<?> scalarType) {
    typeMap.put(scalarType.getType(), scalarType);
    logAdd(scalarType);
  }

  /**
   * Register the ScalarType for an enum. This is special in the sense that an Enum
   * can have many classes if it uses method overrides and we need to register all
   * the variations/classes for the enum.
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public void addEnumType(ScalarType<?> scalarType, Class<? extends Enum> enumClass) {
    Set<Class<?>> mappedClasses = new HashSet<>();
    mappedClasses.add(enumClass);
    for (Object value : EnumSet.allOf(enumClass).toArray()) {
      mappedClasses.add(value.getClass());
    }
    for (Class<?> cls : mappedClasses) {
      typeMap.put(cls, scalarType);
    }
    logAdd(scalarType);
  }

  private void logAdd(ScalarType<?> scalarType) {
    if (log.isTraceEnabled()) {
      String msg = "ScalarType register [" + scalarType.getClass().getName() + "]";
      msg += " for [" + scalarType.getType().getName() + "]";
      log.trace(msg);
    }
  }

  @Override
  public ScalarType<?> getScalarType(String cast) {
    return logicalMap.get(cast);
  }

  /**
   * Return the ScalarType for the given jdbc type as per java.sql.Types.
   */
  @Override
  public ScalarType<?> getScalarType(int jdbcType) {
    return nativeMap.get(jdbcType);
  }

  @Override
  public ScalarType<?> getScalarType(Type propertyType, Class<?> propertyClass) {
    if (propertyType instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType)propertyType;
      Type rawType = pt.getRawType();
      if (List.class == rawType || Set.class == rawType) {
        return getArrayScalarType((Class<?>)rawType, propertyType, true);
      }
    }
    return getScalarType(propertyClass);
  }

  /**
   * This can return null if no matching ScalarType is found.
   */
  @Override
  public ScalarType<?> getScalarType(Class<?> type) {
    ScalarType<?> found = typeMap.get(type);
    if (found == null) {
      if (type.getName().equals("org.joda.time.LocalTime")) {
        throw new IllegalStateException(
          "ScalarType of Joda LocalTime not defined. You need to set DatabaseConfig.jodaLocalTimeMode to"
            + " either 'normal' or 'utc'.  UTC is the old mode using UTC timezone but local time zone is now preferred as 'normal' mode.");
      }
      found = checkInterfaceTypes(type);
    }
    return found;
  }

  private ScalarType<?> checkInterfaceTypes(Class<?> type) {
    if (java.nio.file.Path.class.isAssignableFrom(type)) {
      return typeMap.get(java.nio.file.Path.class);
    }
    return null;
  }

  @Override
  public GeoTypeBinder getGeoTypeBinder() {
    return geoTypeBinder;
  }

  @Override
  public ScalarType<?> getDbMapScalarType() {
    return (postgres) ? hstoreType : ScalarTypeJsonMap.typeFor(false, Types.VARCHAR, false);
  }

  @Override
  public ScalarType<?> getArrayScalarType(Class<?> type, Type genericType, boolean nullable) {
    Type valueType = getValueType(genericType);
    if (type.equals(List.class)) {
      return getArrayScalarTypeList(valueType, nullable);
    } else if (type.equals(Set.class)) {
      return getArrayScalarTypeSet(valueType, nullable);
    } else {
      throw new IllegalStateException("Type [" + type + "] not supported for @DbArray");
    }
  }

  private ScalarType<?> getArrayScalarTypeSet(Type valueType, boolean nullable) {
    if (isEnumType(valueType)) {
      return arrayTypeSetFactory.typeForEnum(createEnumScalarType(asEnumClass(valueType), null), nullable);
    }
    return arrayTypeSetFactory.typeFor(valueType, nullable);
  }

  private ScalarType<?> getArrayScalarTypeList(Type valueType, boolean nullable) {
    if (isEnumType(valueType)) {
      return arrayTypeListFactory.typeForEnum(createEnumScalarType(asEnumClass(valueType), null), nullable);
    }
    return arrayTypeListFactory.typeFor(valueType, nullable);
  }

  private Class<? extends Enum<?>> asEnumClass(Type valueType) {
    return TypeReflectHelper.asEnumClass(valueType);
  }

  private boolean isEnumType(Type valueType) {
    return TypeReflectHelper.isEnumType(valueType);
  }

  @Override
  public ScalarType<?> getJsonScalarType(DeployBeanProperty prop, int dbType, int dbLength) {
    Class<?> type = prop.getPropertyType();
    Type genericType = prop.getGenericType();
    boolean hasJacksonAnnotations = objectMapperPresent && checkJacksonAnnotations(prop);

    if (type.equals(List.class)) {
      DocPropertyType docType = getDocType(genericType);
      if (!hasJacksonAnnotations && isValueTypeSimple(genericType)) {
        return ScalarTypeJsonList.typeFor(postgres, dbType, docType, prop.isNullable(), jsonManager.keepSource(prop));
      } else {
        return createJsonObjectMapperType(prop, dbType, docType);
      }
    }
    if (type.equals(Set.class)) {
      DocPropertyType docType = getDocType(genericType);
      if (!hasJacksonAnnotations && isValueTypeSimple(genericType)) {
        return ScalarTypeJsonSet.typeFor(postgres, dbType, docType, prop.isNullable(), jsonManager.keepSource(prop));
      } else {
        return createJsonObjectMapperType(prop, dbType, docType);
      }
    }
    if (type.equals(Map.class)) {
      if (!hasJacksonAnnotations && isMapValueTypeObject(genericType)) {
        return ScalarTypeJsonMap.typeFor(postgres, dbType, jsonManager.keepSource(prop));
      } else {
        return createJsonObjectMapperType(prop, dbType, DocPropertyType.OBJECT);
      }
    }
    if (objectMapperPresent && prop.getMutationDetection() == MutationDetection.DEFAULT) {
      if (type.equals(JsonNode.class)) {
        switch (dbType) {
          case Types.VARCHAR:
            return jsonNodeVarchar;
          case Types.BLOB:
            return jsonNodeBlob;
          case Types.CLOB:
            return jsonNodeClob;
          case DbPlatformType.JSONB:
            return jsonNodeJsonb;
          default:
            return jsonNodeJson;
        }
      }
    }
    return createJsonObjectMapperType(prop, dbType, DocPropertyType.OBJECT);
  }

  /**
   * Returns TRUE, if there is any jackson annotation on that property. All jackson annotations
   * are annotated with the &#64;JacksonAnnotation meta annotation. So detection is easy.
   */
  private boolean checkJacksonAnnotations(DeployBeanProperty prop) {
    return prop.getMetaAnnotation(com.fasterxml.jackson.annotation.JacksonAnnotation.class) != null;
  }

  private DocPropertyType getDocType(Type genericType) {
    if (genericType instanceof Class<?>) {
      ScalarType<?> found = typeMap.get(genericType);
      if (found != null) {
        return found.getDocType();
      }
    }
    return DocPropertyType.OBJECT;
  }

  /**
   * Return true if value parameter type of the map is Object.
   */
  private boolean isValueTypeSimple(Type collectionType) {
    Type typeArg = TypeReflectHelper.getValueType(collectionType);
    return String.class.equals(typeArg) || Long.class.equals(typeArg);
  }

  private Type getValueType(Type collectionType) {
    return TypeReflectHelper.getValueType(collectionType);
  }

  /**
   * Return true if value parameter type of the map is Object.
   */
  private boolean isMapValueTypeObject(Type genericType) {
    Type[] typeArgs = ((ParameterizedType) genericType).getActualTypeArguments();
    return Object.class.equals(typeArgs[1]) || "?".equals(typeArgs[1].toString());
  }

  private ScalarType<?> createJsonObjectMapperType(DeployBeanProperty prop, int dbType, DocPropertyType docType) {
    Class<?> type = prop.getPropertyType();
    if (objectMapper == null) {
      throw new IllegalArgumentException("Type [" + type + "] unsupported for @DbJson mapping - Jackson ObjectMapper not present");
    }
    return ScalarTypeJsonObjectMapper.createTypeFor(jsonManager, prop, dbType, docType);
  }

  /**
   * Return a ScalarType for a given class.
   * <p>
   * Used for java.util.Date and java.util.Calendar which can be mapped to
   * different jdbcTypes in a single system.
   */
  @Override
  public ScalarType<?> getScalarType(Class<?> type, int jdbcType) {
    // File is a special Lob so check for that first
    if (File.class.equals(type)) {
      return fileType;
    }

    // check for Clob, LongVarchar etc ...
    // the reason being that String maps to multiple jdbc types
    // varchar, clob, longVarchar.
    ScalarType<?> scalarType = getLobTypes(jdbcType);
    if (scalarType != null) {
      // it is a specific Lob type...
      return scalarType;
    }

    scalarType = typeMap.get(type);
    if (scalarType != null) {
      if (jdbcType == 0 || scalarType.getJdbcType() == jdbcType) {
        // matching type
        return scalarType;
      }
    }
    // a util Date with jdbcType not matching server wide settings
    if (type.equals(java.util.Date.class)) {
      return extraTypeFactory.createUtilDate(jsonDateTime, jsonDate, jdbcType);
    }
    // a Calendar with jdbcType not matching server wide settings
    if (type.equals(java.util.Calendar.class)) {
      return extraTypeFactory.createCalendar(jsonDateTime, jdbcType);
    }

    throw new IllegalArgumentException("Unmatched ScalarType for " + type + " jdbcType:" + jdbcType);
  }

  /**
   * Return the types for the known lob types.
   * <p>
   * Kind of special case because these map multiple jdbc types to single Java
   * types - like String - Varchar, LongVarchar, Clob. For this reason I check
   * for the specific Lob types first before looking for a matching type.
   */
  private ScalarType<?> getLobTypes(int jdbcType) {
    return getScalarType(jdbcType);
  }

  /**
   * Convert the Object to the required datatype. The
   *
   * @param value      the Object value
   * @param toJdbcType the type as per java.sql.Types.
   */
  public Object convert(Object value, int toJdbcType) {
    if (value == null) {
      return null;
    }
    ScalarType<?> type = nativeMap.get(toJdbcType);
    if (type != null) {
      return type.toJdbcType(value);
    }
    return value;
  }

  boolean isIntegerType(String s) {
    if (isLeadingZeros(s)) {
      return false;
    }
    try {
      Integer.parseInt(s);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * Treat leading zeros as a non-integer for enum values.
   */
  private boolean isLeadingZeros(String s) {
    return s.length() > 1 && s.charAt(0) == '0';
  }

  /**
   * Create the Mapping of Enum fields to DB values using EnumValue annotations.
   * <p>
   * Return null if the EnumValue annotations are not present/used.
   */
  private ScalarTypeEnum<?> createEnumScalarType2(Class<?> enumType) {
    boolean integerType = true;
    Map<String, String> nameValueMap = new LinkedHashMap<>();
    for (Field field : enumType.getDeclaredFields()) {
      EnumValue enumValue = AnnotationUtil.get(field, EnumValue.class);
      if (enumValue != null) {
        nameValueMap.put(field.getName(), enumValue.value());
        if (integerType && !isIntegerType(enumValue.value())) {
          // will treat the values as strings
          integerType = false;
        }
      }
    }
    if (nameValueMap.isEmpty()) {
      // Not using EnumValue here
      return null;
    }
    return createEnumScalarType(enumType, nameValueMap, integerType, 0, true);
  }

  /**
   * Create a ScalarType for an Enum that has additional mapping.
   * <p>
   * The reason for this is that often in a DB there will be short codes used
   * such as A,I,N rather than the ACTIVE, INACTIVE, NEW. So there really needs
   * to be a mapping from the nicely named enumeration values to the typically
   * much shorter codes used in the DB.
   */
  @Override
  public ScalarType<?> createEnumScalarType(Class<? extends Enum<?>> enumType, EnumType type) {
    ScalarType<?> scalarType = getScalarType(enumType);
    if (scalarType instanceof ScalarTypeWrapper) {
      // no override or further mapping required
      return scalarType;
    }
    ScalarTypeEnum<?> scalarEnum = (ScalarTypeEnum<?>) scalarType;
    if (scalarEnum != null && !scalarEnum.isOverrideBy(type)) {
      if (type != null && !scalarEnum.isCompatible(type)) {
        throw new IllegalStateException("Error mapping Enum type:" + enumType + " It is mapped using 2 different modes when only one is supported (ORDINAL, STRING or an Ebean mapping)");
      }
      return scalarEnum;
    }
    scalarEnum = createEnumScalarTypePerExtentions(enumType);
    if (scalarEnum == null) {
      // use JPA normal Enum type (without mapping)
      scalarEnum = createEnumScalarTypePerSpec(enumType, type);
    }
    addEnumType(scalarEnum, enumType);
    return scalarEnum;
  }

  private ScalarTypeEnum<?> createEnumScalarTypePerSpec(Class<?> enumType, EnumType type) {
    if (type == null) {
      if (defaultEnumType == EnumType.ORDINAL) {
        return new ScalarTypeEnumStandard.OrdinalEnum(enumType);
      } else {
        return new ScalarTypeEnumStandard.StringEnum(enumType);
      }
    } else if (type == EnumType.ORDINAL) {
      return new ScalarTypeEnumStandard.OrdinalEnum(enumType);
    } else {
      return new ScalarTypeEnumStandard.StringEnum(enumType);
    }
  }

  private ScalarTypeEnum<?> createEnumScalarTypePerExtentions(Class<? extends Enum<?>> enumType) {
    for (Method method : enumType.getMethods()) {
      DbEnumValue dbValue = AnnotationUtil.get(method, DbEnumValue.class);
      if (dbValue != null) {
        boolean integerValues = DbEnumType.INTEGER == dbValue.storage();
        return createEnumScalarTypeDbValue(enumType, method, integerValues, dbValue.length(), dbValue.withConstraint());
      }
    }
    // look for EnumValue annotations instead
    return createEnumScalarType2(enumType);
  }

  /**
   * Create the Mapping of Enum fields to DB values using EnumValue annotations.
   * <p>
   * Return null if the EnumValue annotations are not present/used.
   */
  private ScalarTypeEnum<?> createEnumScalarTypeDbValue(Class<? extends Enum<?>> enumType, Method method, boolean integerType, int length, boolean withConstraint) {
    Map<String, String> nameValueMap = new LinkedHashMap<>();
    for (Enum<?> enumConstant : enumType.getEnumConstants()) {
      try {
        Object value = method.invoke(enumConstant);
        nameValueMap.put(enumConstant.name(), value.toString());
      } catch (Exception e) {
        throw new IllegalArgumentException("Error trying to invoke DbEnumValue method on " + enumConstant, e);
      }
    }
    if (nameValueMap.isEmpty()) {
      // Not using EnumValue here
      return null;
    }
    return createEnumScalarType(enumType, nameValueMap, integerType, length, withConstraint);
  }

  /**
   * Given the name value mapping and integer/string type and explicit DB column
   * length create the ScalarType for the Enum.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private ScalarTypeEnum<?> createEnumScalarType(Class enumType, Map<String, String> nameValueMap, boolean integerType, int dbColumnLength, boolean withConstraint) {
    EnumToDbValueMap<?> beanDbMap = EnumToDbValueMap.create(integerType);
    int maxValueLen = 0;
    for (Map.Entry<String, String> entry : nameValueMap.entrySet()) {
      String name = entry.getKey().trim();
      String value = entry.getValue();
      maxValueLen = Math.max(maxValueLen, value.length());
      Object enumValue = Enum.valueOf(enumType, name);
      beanDbMap.add(enumValue, value, name);
    }
    if (dbColumnLength == 0 && !integerType) {
      dbColumnLength = maxValueLen;
    }
    return new ScalarTypeEnumWithMapping(beanDbMap, enumType, dbColumnLength, withConstraint);
  }

  /**
   * Automatically find any ScalarTypes by searching through the class path.
   */
  private void initialiseCustomScalarTypes(BootupClasses bootupClasses) {
    for (Class<? extends ScalarType<?>> cls : bootupClasses.getScalarTypes()) {
      try {
        ScalarType<?> scalarType;
        if (objectMapper == null) {
          scalarType = cls.getDeclaredConstructor().newInstance();
        } else {
          try {
            // first try objectMapper constructor
            scalarType = cls.getDeclaredConstructor(ObjectMapper.class).newInstance(objectMapper);
          } catch (NoSuchMethodException e) {
            scalarType = cls.getDeclaredConstructor().newInstance();
          }
        }
        addCustomType(scalarType);
      } catch (Exception e) {
        log.error("Error loading ScalarType [" + cls.getName() + "]", e);
      }
    }
  }

  private void addCustomType(ScalarType<?> scalarType) {
    add(scalarType);
  }

  private Object initObjectMapper(DatabaseConfig config) {
    Object objectMapper = config.getObjectMapper();
    if (objectMapper == null) {
      objectMapper = InitObjectMapper.init();
      config.setObjectMapper(objectMapper);
    }
    return objectMapper;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void initialiseScalarConverters(BootupClasses bootupClasses) {
    for (Class<? extends ScalarTypeConverter<?, ?>> foundType : bootupClasses.getScalarConverters()) {
      try {
        Class<?>[] paramTypes = TypeReflectHelper.getParams(foundType, ScalarTypeConverter.class);
        if (paramTypes.length != 2) {
          throw new IllegalStateException("Expected 2 generics paramtypes but got: " + Arrays.toString(paramTypes));
        }
        Class<?> logicalType = paramTypes[0];
        Class<?> persistType = paramTypes[1];
        ScalarType<?> wrappedType = getScalarType(persistType);
        if (wrappedType == null) {
          throw new IllegalStateException("Could not find ScalarType for: " + paramTypes[1]);
        }
        ScalarTypeConverter converter = foundType.getDeclaredConstructor().newInstance();
        ScalarTypeWrapper stw = new ScalarTypeWrapper(logicalType, wrappedType, converter);
        log.debug("Register ScalarTypeWrapper from {} -> {} using:{}", logicalType, persistType, foundType);
        add(stw);
      } catch (Exception e) {
        log.error("Error registering ScalarTypeConverter [" + foundType.getName() + "]", e);
      }
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void initialiseAttributeConverters(BootupClasses bootupClasses) {
    for (Class<? extends AttributeConverter<?, ?>> foundType : bootupClasses.getAttributeConverters()) {
      try {
        Class<?>[] paramTypes = TypeReflectHelper.getParams(foundType, AttributeConverter.class);
        if (paramTypes.length != 2) {
          throw new IllegalStateException("Expected 2 generics paramtypes but got: " + Arrays.toString(paramTypes));
        }
        Class<?> logicalType = paramTypes[0];
        Class<?> persistType = paramTypes[1];
        ScalarType<?> wrappedType = getScalarType(persistType);
        if (wrappedType == null) {
          throw new IllegalStateException("Could not find ScalarType for: " + paramTypes[1]);
        }
        AttributeConverter converter = foundType.getDeclaredConstructor().newInstance();
        ScalarTypeWrapper stw = new ScalarTypeWrapper(logicalType, wrappedType, new AttributeConverterAdapter(converter));
        log.debug("Register ScalarTypeWrapper from {} -> {} using:{}", logicalType, persistType, foundType);
        add(stw);
      } catch (Exception e) {
        log.error("Error registering AttributeConverter [" + foundType.getName() + "]", e);
      }
    }
  }

  /**
   * Add support for Jackson's JsonNode mapping to Clob, Blob, Varchar, JSON and JSONB.
   */
  private void initialiseJacksonTypes(DatabaseConfig config) {
    if (objectMapper != null) {
      log.trace("Registering JsonNode type support");
      ObjectMapper mapper = (ObjectMapper) objectMapper;
      jsonNodeClob = new ScalarTypeJsonNode.Clob(mapper);
      jsonNodeBlob = new ScalarTypeJsonNode.Blob(mapper);
      jsonNodeVarchar = new ScalarTypeJsonNode.Varchar(mapper);
      jsonNodeJson = jsonNodeClob;  // Default for non-Postgres databases
      jsonNodeJsonb = jsonNodeClob; // Default for non-Postgres databases
      if (isPostgres(config.getDatabasePlatform())) {
        jsonNodeJson = new ScalarTypeJsonNodePostgres.JSON(mapper);
        jsonNodeJsonb = new ScalarTypeJsonNodePostgres.JSONB(mapper);
      }
      // add as default mapping for JsonNode (when not annotated with @DbJson etc)
      typeMap.put(JsonNode.class, jsonNodeJson);
    }
  }

  private void initialiseJavaTimeTypes(DatabaseConfig config) {

    ZoneId zoneId = getZoneId(config);

    typeMap.put(java.nio.file.Path.class, new ScalarTypePath());
    addType(java.time.Period.class, new ScalarTypePeriod());
    addType(java.time.LocalDate.class, new ScalarTypeLocalDate(jsonDate));
    addType(java.time.LocalDateTime.class, new ScalarTypeLocalDateTime(jsonDateTime));
    addType(OffsetDateTime.class, new ScalarTypeOffsetDateTime(jsonDateTime, zoneId));
    addType(ZonedDateTime.class, new ScalarTypeZonedDateTime(jsonDateTime, zoneId));
    addType(Instant.class, new ScalarTypeInstant(jsonDateTime));
    addType(DayOfWeek.class, new ScalarTypeDayOfWeek());
    addType(Month.class, new ScalarTypeMonth());
    addType(Year.class, new ScalarTypeYear());
    addType(YearMonth.class, new ScalarTypeYearMonthDate(jsonDate));
    addType(MonthDay.class, new ScalarTypeMonthDay());
    addType(OffsetTime.class, new ScalarTypeOffsetTime());
    addType(ZoneId.class, new ScalarTypeZoneId());
    addType(ZoneOffset.class, new ScalarTypeZoneOffset());
    boolean localTimeNanos = config.isLocalTimeWithNanos();
    addType(java.time.LocalTime.class, (localTimeNanos) ? new ScalarTypeLocalTimeWithNanos() : new ScalarTypeLocalTime());
    boolean durationNanos = config.isDurationWithNanos();
    addType(Duration.class, (durationNanos) ? new ScalarTypeDurationWithNanos() : new ScalarTypeDuration());
  }

  private ZoneId getZoneId(DatabaseConfig config) {
    final String dataTimeZone = config.getDataTimeZone();
    return (dataTimeZone == null) ? ZoneOffset.systemDefault() : TimeZone.getTimeZone(dataTimeZone).toZoneId();
  }

  private void addType(Class<?> clazz, ScalarType<?> scalarType) {
    typeMap.put(clazz, scalarType);
    logicalMap.putIfAbsent(clazz.getSimpleName(), scalarType);
  }

  /**
   * Detect if Joda classes are in the classpath and if so register the Joda data types.
   */
  @SuppressWarnings("deprecation")
  private void initialiseJodaTypes(DatabaseConfig config) {
    // detect if Joda classes are in the classpath
    if (config.getClassLoadConfig().isJodaTimePresent()) {
      // Joda classes are in the classpath so register the types
      log.debug("Registering Joda data types");
      addType(LocalDateTime.class, new ScalarTypeJodaLocalDateTime(jsonDateTime));
      addType(DateTime.class, new ScalarTypeJodaDateTime(jsonDateTime));
      addType(LocalDate.class, new ScalarTypeJodaLocalDate(jsonDate));
      addType(org.joda.time.DateMidnight.class, new ScalarTypeJodaDateMidnight(jsonDate));
      addType(org.joda.time.Period.class, new ScalarTypeJodaPeriod());

      String jodaLocalTimeMode = config.getJodaLocalTimeMode();
      if ("normal".equalsIgnoreCase(jodaLocalTimeMode)) {
        // use the expected/normal local time zone
        addType(LocalTime.class, new ScalarTypeJodaLocalTime());
        log.debug("registered ScalarTypeJodaLocalTime");
      } else if ("utc".equalsIgnoreCase(jodaLocalTimeMode)) {
        // use the old UTC based
        addType(LocalTime.class, new ScalarTypeJodaLocalTimeUTC());
        log.debug("registered ScalarTypeJodaLocalTimeUTC");
      }
    }
  }

  /**
   * Register all the standard types supported. This is the standard JDBC types
   * plus some other common types such as java.util.Date and java.util.Calendar.
   */
  private void initialiseStandard(DatabaseConfig config) {
    DatabasePlatform databasePlatform = config.getDatabasePlatform();
    int platformClobType = databasePlatform.getClobDbType();
    int platformBlobType = databasePlatform.getBlobDbType();

    nativeMap.put(DbPlatformType.HSTORE, hstoreType);

    addType(java.util.Date.class, extraTypeFactory.createUtilDate(jsonDateTime, jsonDate));
    addType(Calendar.class, extraTypeFactory.createCalendar(jsonDateTime));
    addType(BigInteger.class, extraTypeFactory.createMathBigInteger());

    ScalarTypeBool booleanType = extraTypeFactory.createBoolean();
    addType(Boolean.class, booleanType);
    addType(boolean.class, booleanType);
    // register the boolean literals to the platform for DDL default values
    databasePlatform.setDbTrueLiteral(booleanType.getDbTrueLiteral());
    databasePlatform.setDbFalseLiteral(booleanType.getDbFalseLiteral());
    // always register Types.BOOLEAN to our boolean type
    nativeMap.put(Types.BOOLEAN, booleanType);
    if (booleanType.getJdbcType() == Types.BIT) {
      // for MapBeans ... BIT types are assumed to be booleans
      nativeMap.put(Types.BIT, booleanType);
    }

    PlatformConfig.DbUuid dbUuid = config.getPlatformConfig().getDbUuid();
    if (offlineMigrationGeneration || (databasePlatform.isNativeUuidType() && dbUuid.useNativeType())) {
      addType(UUID.class, new ScalarTypeUUIDNative());
    } else {
      // Store UUID as binary(16) or varchar(40)
      ScalarType<?> uuidType = dbUuid.useBinary() ? new ScalarTypeUUIDBinary(dbUuid.useBinaryOptimized()) : new ScalarTypeUUIDVarchar();
      addType(UUID.class, uuidType);
    }

    if (offlineMigrationGeneration || (postgres && !config.getPlatformConfig().isDatabaseInetAddressVarchar())) {
      addInetAddressType(new ScalarTypeInetAddressPostgres());
    } else {
      addInetAddressType(new ScalarTypeInetAddress());
    }

    if (offlineMigrationGeneration || postgres) {
      addType(Cidr.class, new ScalarTypeCidr.Postgres());
      addType(Inet.class, new ScalarTypeInet.Postgres());
    } else {
      addType(Cidr.class, new ScalarTypeCidr.Varchar());
      addType(Inet.class, new ScalarTypeInet.Varchar());
    }

    addType(File.class, fileType);
    addType(Locale.class, localeType);
    addType(Currency.class, currencyType);
    addType(TimeZone.class, timeZoneType);
    addType(URL.class, urlType);
    addType(URI.class, uriType);

    // String types
    addType(char[].class, charArrayType);
    addType(char.class, charType);
    addType(String.class, stringType);
    nativeMap.put(Types.VARCHAR, stringType);
    nativeMap.put(Types.CHAR, stringType);
    nativeMap.put(Types.LONGVARCHAR, longVarcharType);

    // Class<?>
    addType(Class.class, classType);

    if (platformClobType == Types.CLOB) {
      nativeMap.put(Types.CLOB, clobType);
    } else {
      // for Postgres Clobs handled by Varchar ScalarType...
      ScalarType<?> platClobScalarType = nativeMap.get(platformClobType);
      if (platClobScalarType == null) {
        throw new IllegalArgumentException("Type for dbPlatform clobType [" + clobType + "] not found.");
      }
      nativeMap.put(Types.CLOB, platClobScalarType);
    }

    // Binary type
    addType(byte[].class, varbinaryType);
    nativeMap.put(Types.BINARY, binaryType);
    nativeMap.put(Types.VARBINARY, varbinaryType);
    nativeMap.put(Types.LONGVARBINARY, longVarbinaryType);

    if (platformBlobType == Types.BLOB) {
      nativeMap.put(Types.BLOB, blobType);
    } else {
      // for Postgres Blobs handled by LongVarbinary ScalarType...
      ScalarType<?> platBlobScalarType = nativeMap.get(platformBlobType);
      if (platBlobScalarType == null) {
        throw new IllegalArgumentException("Type for dbPlatform blobType [" + blobType + "] not found.");
      }
      nativeMap.put(Types.BLOB, platBlobScalarType);
    }

    // Number types
    addType(Byte.class, byteType);
    addType(byte.class, byteType);
    nativeMap.put(Types.TINYINT, byteType);

    addType(Short.class, shortType);
    addType(short.class, shortType);
    nativeMap.put(Types.SMALLINT, shortType);

    addType(Integer.class, integerType);
    addType(int.class, integerType);
    nativeMap.put(Types.INTEGER, integerType);

    addType(Long.class, longType);
    addType(long.class, longType);
    nativeMap.put(Types.BIGINT, longType);

    addType(Double.class, doubleType);
    addType(double.class, doubleType);
    nativeMap.put(Types.FLOAT, doubleType);// no this is not a bug
    nativeMap.put(Types.DOUBLE, doubleType);

    addType(Float.class, floatType);
    addType(float.class, floatType);
    nativeMap.put(Types.REAL, floatType);// no this is not a bug

    addType(BigDecimal.class, bigDecimalType);
    nativeMap.put(Types.DECIMAL, bigDecimalType);
    nativeMap.put(Types.NUMERIC, bigDecimalType);

    // Temporal types
    addType(Time.class, timeType);
    nativeMap.put(Types.TIME, timeType);

    ScalarTypeDate dateType = new ScalarTypeDate(jsonDate);
    addType(Date.class, dateType);
    nativeMap.put(Types.DATE, dateType);

    ScalarType<?> timestampType = new ScalarTypeTimestamp(jsonDateTime);
    addType(Timestamp.class, timestampType);
    nativeMap.put(Types.TIMESTAMP, timestampType);
  }

  @SuppressWarnings("rawtypes")
  private void addInetAddressType(ScalarType scalarType) {
    addType(InetAddress.class, scalarType);
    addType(Inet4Address.class, scalarType);
    addType(Inet6Address.class, scalarType);
  }

}
