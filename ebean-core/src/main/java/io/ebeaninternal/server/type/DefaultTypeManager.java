package io.ebeaninternal.server.type;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ebean.DatabaseBuilder;
import io.ebean.annotation.*;
import io.ebean.config.*;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.core.type.*;
import io.ebean.lookup.Lookups;
import io.ebean.types.Cidr;
import io.ebean.types.Inet;
import io.ebean.util.AnnotationUtil;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.api.DbOffline;
import io.ebeaninternal.api.GeoTypeProvider;
import io.ebeaninternal.server.core.ServiceUtil;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.EnumType;
import java.io.File;
import java.lang.invoke.MethodType;
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

import static io.ebean.core.type.PostgresHelper.isPostgresCompatible;
import static java.lang.System.Logger.Level.*;

/**
 * Default implementation of TypeManager.
 * <p>
 * Manages the list of ScalarType that is available.
 */
public final class DefaultTypeManager implements TypeManager {

  private static final System.Logger log = CoreLog.internal;

  private final Map<Class<?>, ScalarTypeSet<?>> typeSets = new HashMap<>();
  private final ConcurrentHashMap<Class<?>, ScalarType<?>> typeMap;
  private final ConcurrentHashMap<Integer, ScalarType<?>> nativeMap;
  private final ConcurrentHashMap<String, ScalarType<?>> logicalMap;

  private final DefaultTypeFactory extraTypeFactory;

  private final ScalarType<?> fileType = new ScalarTypeFile();
  private final ScalarType<?> hstoreType = new ScalarTypePostgresHstore();

  private final JsonConfig.DateTime jsonDateTime;
  private final JsonConfig.Date jsonDate;

  private final Object objectMapper;
  private final boolean objectMapperPresent;
  private final boolean postgres;
  private final ScalarJsonManager jsonManager;
  private final boolean offlineMigrationGeneration;
  private final EnumType defaultEnumType;
  private final DatabasePlatform databasePlatform;

  private final PlatformArrayTypeFactory arrayTypeListFactory;
  private final PlatformArrayTypeFactory arrayTypeSetFactory;
  private final ScalarJsonMapper jsonMapper;
  private GeoTypeBinder geoTypeBinder;

  /**
   * Create the DefaultTypeManager.
   */
  public DefaultTypeManager(DatabaseBuilder.Settings config, BootupClasses bootupClasses) {
    this.jsonDateTime = config.getJsonDateTime();
    this.jsonDate = config.getJsonDate();
    this.typeMap = new ConcurrentHashMap<>();
    this.nativeMap = new ConcurrentHashMap<>();
    this.logicalMap = new ConcurrentHashMap<>();
    this.databasePlatform = config.getDatabasePlatform();
    this.postgres = isPostgresCompatible(config.getDatabasePlatform());
    this.objectMapperPresent = config.getClassLoadConfig().isJacksonObjectMapperPresent();
    this.objectMapper = (objectMapperPresent) ? initObjectMapper(config) : null;
    this.jsonManager = (objectMapperPresent) ? new TypeJsonManager(postgres, objectMapper, config.getJsonMutationDetection()) : null;
    this.extraTypeFactory = new DefaultTypeFactory(config);
    this.arrayTypeListFactory = arrayTypeListFactory(config.getDatabasePlatform());
    this.arrayTypeSetFactory = arrayTypeSetFactory(config.getDatabasePlatform());
    this.offlineMigrationGeneration = DbOffline.isGenerateMigration();
    this.defaultEnumType = config.getDefaultEnumType();

    ServiceLoader<ScalarJsonMapper> mappers = ServiceLoader.load(ScalarJsonMapper.class);
    jsonMapper = mappers.findFirst().orElse(null);

    initialiseStandard(config);
    initialiseJavaTimeTypes(config);
    loadTypesFromProviders(config, objectMapper);
    loadGeoTypeBinder(config);

    if (bootupClasses != null) {
      initialiseCustomScalarTypes(bootupClasses);
      initialiseScalarConverters(bootupClasses);
      initialiseAttributeConverters(bootupClasses);
    }
  }

  private void loadGeoTypeBinder(DatabaseBuilder.Settings config) {
    GeoTypeProvider provider = config.getServiceObject(GeoTypeProvider.class);
    if (provider == null) {
      provider = ServiceUtil.service(GeoTypeProvider.class);
    }
    if (provider != null) {
      geoTypeBinder = provider.createBinder(config);
    }
  }

  private PlatformArrayTypeFactory arrayTypeListFactory(DatabasePlatform databasePlatform) {
    if (databasePlatform.nativeArrayType()) {
      return ScalarTypeArrayList.factory();
    } else if (databasePlatform.isPlatform(Platform.H2)) {
      return ScalarTypeArrayListH2.factory();
    }
    return new PlatformArrayTypeJsonList();
  }

  private PlatformArrayTypeFactory arrayTypeSetFactory(DatabasePlatform databasePlatform) {
    if (databasePlatform.nativeArrayType()) {
      return ScalarTypeArraySet.factory();
    } else if (databasePlatform.isPlatform(Platform.H2)) {
      return ScalarTypeArraySetH2.factory();
    }
    return new PlatformArrayTypeJsonSet();
  }

  /**
   * Load custom scalar types registered via ExtraTypeFactory and ServiceLoader.
   */
  private void loadTypesFromProviders(DatabaseBuilder.Settings config, Object objectMapper) {
    for (ExtraTypeFactory plugin : ServiceLoader.load(ExtraTypeFactory.class)) {
      for (ScalarType<?> type : plugin.createTypes(config, objectMapper)) {
        add(type);
      }
    }
    for (ScalarTypeSetFactory factory : ServiceLoader.load(ScalarTypeSetFactory.class)) {
      ScalarTypeSet<?> typeSet = factory.createTypeSet(config, objectMapper);
      if (typeSet != null) {
        typeSets.put(typeSet.type(), typeSet);
        ScalarType<?> defaultType = typeSet.defaultType();
        if (defaultType != null) {
          typeMap.put(typeSet.type(), defaultType);
        }
      }
    }
  }

  private boolean hstoreSupport() {
    return databasePlatform.isPlatform(Platform.POSTGRES);
  }

  /**
   * Register a custom ScalarType.
   */
  private void add(ScalarType<?> scalarType) {
    typeMap.put(scalarType.type(), scalarType);
    logAdd(scalarType);
  }

  private void logAdd(ScalarType<?> scalarType) {
    if (log.isLoggable(TRACE)) {
      log.log(TRACE, "ScalarType register {0} for {1}", scalarType.getClass().getName(), scalarType.type().getName());
    }
  }

  @Override
  public ScalarType<?> type(String cast) {
    return logicalMap.get(cast);
  }

  /**
   * Return the ScalarType for the given jdbc type as per java.sql.Types.
   */
  @Override
  public ScalarType<?> type(int jdbcType) {
    return nativeMap.get(jdbcType);
  }

  @Override
  public ScalarType<?> type(Type propertyType, Class<?> propertyClass) {
    if (propertyType instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) propertyType;
      Type rawType = pt.getRawType();
      if (List.class == rawType || Set.class == rawType) {
        return dbArrayType((Class<?>) rawType, propertyType, true);
      }
    }
    return type(propertyClass);
  }

  /**
   * This can return null if no matching ScalarType is found.
   */
  @Override
  public ScalarType<?> type(Class<?> type) {
    ScalarType<?> found = typeMap.get(type);
    if (found == null) {
      if (type.getName().equals("org.joda.time.LocalTime")) {
        throw new IllegalStateException(
          "ScalarType of Joda LocalTime not defined. 1) Check ebean-joda-time dependency has been added  2) Check DatabaseConfig.jodaLocalTimeMode is set to"
            + " either 'normal' or 'utc'.  UTC is the old mode using UTC timezone but local time zone is now preferred as 'normal' mode.");
      }
      found = checkInheritedTypes(type);
    }
    return found != ScalarTypeNotFound.INSTANCE ? found : null; // Do not return ScalarTypeNotFound, otherwise checks will fail
  }

  /**
   * Checks the typeMap for inherited types.
   * <p>
   * If e.g. <code>type</code> is a <code>GregorianCalendar</code>, then this method
   * will check the class hierarchy and will probably return a
   * <code>ScalarTypeCalendar</code> To speed up a second lookup, it will write
   * back the found scalarType to typeMap.
   *
   * @param type the for which to search for a <code>ScalarType</code>
   * @return either a valid <code>ScalarType</code> if one could be found or {@link ScalarTypeNotFound#INSTANCE} if not
   */
  private ScalarType<?> checkInheritedTypes(Class<?> type) {
    // first step loop through inheritance chain
    Class<?> parent = type;
    while (parent != null && parent != Object.class) {
      ScalarType<?> found = typeMap.get(parent);
      if (found != null && found != ScalarTypeNotFound.INSTANCE) {
        typeMap.put(type, found); // store type for next lookup
        return found;
      }
      // second step - loop through interfaces of this type
      for (Class<?> iface : parent.getInterfaces()) {
        found = checkInheritedTypes(iface);
        if (found != null && found != ScalarTypeNotFound.INSTANCE) {
          typeMap.put(type, found); // store type for next lookup
          return found;
        }
      }
      parent = parent.getSuperclass();
    }
    typeMap.put(type, ScalarTypeNotFound.INSTANCE);
    return ScalarTypeNotFound.INSTANCE; // no success
  }

  @Override
  public GeoTypeBinder geoTypeBinder() {
    return geoTypeBinder;
  }

  @Override
  public ScalarType<?> dbMapType() {
    return hstoreSupport() ? hstoreType : ScalarTypeJsonMap.typeFor(false, Types.VARCHAR, false);
  }

  @Override
  public ScalarType<?> dbArrayType(Class<?> type, Type genericType, boolean nullable) {
    Type valueType = valueType(genericType);
    if (type.equals(List.class)) {
      return dbArrayTypeList(valueType, nullable);
    } else if (type.equals(Set.class)) {
      return dbArrayTypeSet(valueType, nullable);
    } else {
      throw new IllegalStateException("@DbArray does not support type " + type);
    }
  }

  private ScalarType<?> dbArrayTypeSet(Type valueType, boolean nullable) {
    if (isEnumType(valueType)) {
      return arrayTypeSetFactory.typeForEnum(enumType(asEnumClass(valueType), null), nullable);
    }
    return arrayTypeSetFactory.typeFor(valueType, nullable);
  }

  private ScalarType<?> dbArrayTypeList(Type valueType, boolean nullable) {
    if (isEnumType(valueType)) {
      return arrayTypeListFactory.typeForEnum(enumType(asEnumClass(valueType), null), nullable);
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
  public ScalarType<?> dbJsonType(DeployBeanProperty prop, int dbType, int dbLength) {
    Class<?> type = prop.getPropertyType();
    if (type.equals(String.class)) {
      return ScalarTypeJsonString.typeFor(postgres, dbType);
    }
    if (jsonMapper != null) {
      var markerAnnotation = jsonMapper.markerAnnotation();
      if (markerAnnotation != null && !prop.getMetaAnnotations(markerAnnotation).isEmpty()) {
        return createJsonObjectMapperType(prop, dbType, docPropertyType(prop, type));
      }
    }
    Type genericType = prop.getGenericType();
    if (type.equals(List.class) && isValueTypeSimple(genericType)) {
      return ScalarTypeJsonList.typeFor(postgres, dbType, docType(genericType), prop.isNullable(), keepSource(prop));
    }
    if (type.equals(Set.class) && isValueTypeSimple(genericType)) {
      return ScalarTypeJsonSet.typeFor(postgres, dbType, docType(genericType), prop.isNullable(), keepSource(prop));
    }
    if (type.equals(Map.class) && isMapValueTypeObject(genericType)) {
      return ScalarTypeJsonMap.typeFor(postgres, dbType, keepSource(prop));
    }
    if (objectMapperPresent && prop.getMutationDetection() == MutationDetection.DEFAULT) {
      ScalarTypeSet<?> typeSet = typeSets.get(type);
      if (typeSet != null) {
        return typeSet.forType(dbType);
      }
    }
    return createJsonObjectMapperType(prop, dbType, DocPropertyType.OBJECT);
  }

  private boolean keepSource(DeployBeanProperty prop) {
    if (prop.getMutationDetection() == MutationDetection.DEFAULT) {
      prop.setMutationDetection(jsonManager.mutationDetection());
    }
    return prop.getMutationDetection() == MutationDetection.SOURCE;
  }

  private DocPropertyType docPropertyType(DeployBeanProperty prop, Class<?> type) {
    return type.equals(List.class) || type.equals(Set.class) ? docType(prop.getGenericType()) : DocPropertyType.OBJECT;
  }

  private DocPropertyType docType(Type genericType) {
    if (genericType instanceof Class<?>) {
      ScalarType<?> found = type((Class<?>) genericType);
      if (found != null) {
        return found.docType();
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

  private Type valueType(Type collectionType) {
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
    if (jsonMapper == null) {
      throw new IllegalArgumentException("Unsupported @DbJson mapping - Jackson ObjectMapper not present for " + prop);
    }
    if (MutationDetection.DEFAULT == prop.getMutationDetection()) {
      prop.setMutationDetection(jsonManager.mutationDetection());
    }
    var req = new ScalarJsonRequest(jsonManager, dbType, docType, prop.getDesc().getBeanType(), prop.getMutationDetection(), prop.getName());
    return jsonMapper.createType(req);
  }

  /**
   * Return a ScalarType for a given class.
   * <p>
   * Used for java.util.Date and java.util.Calendar which can be mapped to
   * different jdbcTypes in a single system.
   */
  @Override
  public ScalarType<?> type(Class<?> type, int jdbcType) {
    // File is a special Lob so check for that first
    if (File.class.equals(type)) {
      return fileType;
    }

    // check for Clob, LongVarchar etc ...
    // the reason being that String maps to multiple jdbc types
    // varchar, clob, longVarchar.
    ScalarType<?> scalarType = lobTypes(jdbcType);
    if (scalarType != null) {
      // it is a specific Lob type...
      return scalarType;
    }

    scalarType = type(type);
    if (scalarType != null) {
      if (jdbcType == 0 || scalarType.jdbcType() == jdbcType) {
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
  private ScalarType<?> lobTypes(int jdbcType) {
    return type(jdbcType);
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
  private ScalarTypeEnum<?> enumTypeEnumValue(Class<?> enumType) {
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
  public ScalarType<?> enumType(Class<? extends Enum<?>> enumType, EnumType type) {
    ScalarType<?> scalarType = type(enumType);
    if (scalarType instanceof ScalarTypeWrapper) {
      // no override or further mapping required
      return scalarType;
    }
    ScalarTypeEnum<?> scalarEnum = (ScalarTypeEnum<?>) scalarType;
    if (scalarEnum != null && !scalarEnum.isOverrideBy(type)) {
      if (type != null && !scalarEnum.isCompatible(type)) {
        throw new IllegalStateException("Error mapping Enum type:" + enumType + " It is mapped using 2 of (ORDINAL, STRING or an Ebean mapping) when only one is supported.");
      }
      return scalarEnum;
    }
    scalarEnum = enumTypePerExtensions(enumType);
    if (scalarEnum == null) {
      // use JPA normal Enum type (without mapping)
      scalarEnum = enumTypePerSpec(enumType, type);
    }
    add(scalarEnum);
    return scalarEnum;
  }

  private ScalarTypeEnum<?> enumTypePerSpec(Class<?> enumType, EnumType type) {
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

  private ScalarTypeEnum<?> enumTypePerExtensions(Class<? extends Enum<?>> enumType) {
    for (Method method : enumType.getMethods()) {
      DbEnumValue dbValue = AnnotationUtil.get(method, DbEnumValue.class);
      if (dbValue != null) {
        boolean integerValues = DbEnumType.INTEGER == dbValue.storage();
        return enumTypeDbValue(enumType, method, integerValues, dbValue.length(), dbValue.withConstraint());
      }
    }
    // look for EnumValue annotations instead
    return enumTypeEnumValue(enumType);
  }

  /**
   * Create the Mapping of Enum fields to DB values using EnumValue annotations.
   * <p>
   * Return null if the EnumValue annotations are not present/used.
   */
  private ScalarTypeEnum<?> enumTypeDbValue(Class<? extends Enum<?>> enumType, Method method, boolean integerType, int length, boolean withConstraint) {
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
        var lookup = Lookups.getLookup(cls);
        ScalarType<?> scalarType;
        if (objectMapper == null) {
          scalarType =
              (ScalarType<?>)
                  lookup.findConstructor(cls, MethodType.methodType(void.class)).invoke();
        } else {
          try {
            // first try objectMapper constructor
            scalarType =
                (ScalarType<?>)
                    lookup
                        .findConstructor(cls, MethodType.methodType(ObjectMapper.class))
                        .invoke(objectMapper);
          } catch (NoSuchMethodException e) {
            scalarType =
                (ScalarType<?>)
                    lookup.findConstructor(cls, MethodType.methodType(void.class)).invoke();
          }
        }
        add(scalarType);
      } catch (Throwable e) {
        log.log(ERROR, "Error loading ScalarType " + cls.getName(), e);
      }
    }
  }


  private Object initObjectMapper(DatabaseBuilder.Settings config) {
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
        ScalarType<?> wrappedType = type(persistType);
        if (wrappedType == null) {
          throw new IllegalStateException("Could not find ScalarType for: " + paramTypes[1]);
        }
        ScalarTypeConverter converter = Lookups.newDefaultInstance(foundType);
        ScalarTypeWrapper stw = new ScalarTypeWrapper(logicalType, wrappedType, converter);
        log.log(DEBUG, "Register ScalarTypeWrapper from {0} -> {1} using:{2}", logicalType, persistType, foundType);
        add(stw);
      } catch (Throwable e) {
        log.log(ERROR, "Error registering ScalarTypeConverter " + foundType.getName(), e);
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
        ScalarType<?> wrappedType = type(persistType);
        if (wrappedType == null) {
          throw new IllegalStateException("Could not find ScalarType for: " + paramTypes[1]);
        }
        AttributeConverter converter =  Lookups.newDefaultInstance(foundType);
        ScalarTypeWrapper stw = new ScalarTypeWrapper(logicalType, wrappedType, new AttributeConverterAdapter(converter));
        log.log(DEBUG, "Register ScalarTypeWrapper from {0} -> {1} using:{2}", logicalType, persistType, foundType);
        add(stw);
      } catch (Throwable e) {
        log.log(ERROR, "Error registering AttributeConverter " + foundType.getName(), e);
      }
    }
  }


  private void initialiseJavaTimeTypes(DatabaseBuilder.Settings config) {
    ZoneId zoneId = zoneId(config);

    typeMap.put(java.nio.file.Path.class, new ScalarTypePath());
    addType(java.time.Period.class, new ScalarTypePeriod());
    if (config.getDatabasePlatform().supportsNativeJavaTime()) {
      addType(java.time.LocalDate.class, new ScalarTypeLocalDateNative(jsonDate));
    } else {
      addType(java.time.LocalDate.class, new ScalarTypeLocalDate(jsonDate));
    }
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

  private ZoneId zoneId(DatabaseBuilder.Settings config) {
    final String dataTimeZone = config.getDataTimeZone();
    return (dataTimeZone == null) ? ZoneOffset.systemDefault() : TimeZone.getTimeZone(dataTimeZone).toZoneId();
  }

  private void addType(Class<?> clazz, ScalarType<?> scalarType) {
    typeMap.put(clazz, scalarType);
    logicalMap.putIfAbsent(clazz.getSimpleName(), scalarType);
  }

  /**
   * Register all the standard types supported. This is the standard JDBC types
   * plus some other common types such as java.util.Date and java.util.Calendar.
   */
  private void initialiseStandard(DatabaseBuilder.Settings config) {
    DatabasePlatform databasePlatform = config.getDatabasePlatform();
    int platformClobType = databasePlatform.clobDbType();
    int platformBlobType = databasePlatform.blobDbType();

    nativeMap.put(DbPlatformType.HSTORE, hstoreType);

    addType(java.util.Date.class, extraTypeFactory.createUtilDate(jsonDateTime, jsonDate));
    addType(Calendar.class, extraTypeFactory.createCalendar(jsonDateTime));
    addType(BigInteger.class, extraTypeFactory.createMathBigInteger());

    final var booleanType = extraTypeFactory.createBoolean();
    addType(Boolean.class, booleanType);
    addType(boolean.class, booleanType);
    // register the boolean literals to the platform for DDL default values
    databasePlatform.setDbTrueLiteral(booleanType.getDbTrueLiteral());
    databasePlatform.setDbFalseLiteral(booleanType.getDbFalseLiteral());
    // always register Types.BOOLEAN to our boolean type
    nativeMap.put(Types.BOOLEAN, booleanType);
    if (booleanType.jdbcType() == Types.BIT) {
      // for MapBeans ... BIT types are assumed to be booleans
      nativeMap.put(Types.BIT, booleanType);
    }

    PlatformConfig.DbUuid dbUuid = config.getPlatformConfig().getDbUuid();
    if (offlineMigrationGeneration || (databasePlatform.nativeUuidType() && dbUuid.useNativeType())) {
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
    addType(Locale.class, new ScalarTypeLocale());
    addType(Currency.class, new ScalarTypeCurrency());
    addType(TimeZone.class, new ScalarTypeTimeZone());
    addType(URL.class, new ScalarTypeURL());
    addType(URI.class, new ScalarTypeURI());

    // String types
    addType(char[].class, new ScalarTypeCharArray());
    addType(char.class, new ScalarTypeChar());
    addType(String.class, ScalarTypeString.INSTANCE);
    nativeMap.put(Types.VARCHAR, ScalarTypeString.INSTANCE);
    nativeMap.put(Types.CHAR, ScalarTypeString.INSTANCE);
    nativeMap.put(Types.LONGVARCHAR, new ScalarTypeLongVarchar());

    addType(Class.class, new ScalarTypeClass());

    if (platformClobType == Types.CLOB) {
      nativeMap.put(Types.CLOB, new ScalarTypeClob());
    } else {
      // for Postgres Clobs handled by Varchar ScalarType...
      ScalarType<?> platClobScalarType = nativeMap.get(platformClobType);
      if (platClobScalarType == null) {
        throw new IllegalArgumentException("Not found dbPlatform clobType " + platformClobType);
      }
      nativeMap.put(Types.CLOB, platClobScalarType);
    }

    // Binary type
    final var varbinaryType = new ScalarTypeBytesVarbinary();
    addType(byte[].class, varbinaryType);
    nativeMap.put(Types.VARBINARY, varbinaryType);
    nativeMap.put(Types.BINARY, new ScalarTypeBytesBinary());
    nativeMap.put(Types.LONGVARBINARY, new ScalarTypeBytesLongVarbinary());

    if (platformBlobType == Types.BLOB) {
      nativeMap.put(Types.BLOB, new ScalarTypeBytesBlob());
    } else {
      // for Postgres Blobs handled by LongVarbinary ScalarType...
      ScalarType<?> platBlobScalarType = nativeMap.get(platformBlobType);
      if (platBlobScalarType == null) {
        throw new IllegalArgumentException("Not found dbPlatform blobType " + platformBlobType);
      }
      nativeMap.put(Types.BLOB, platBlobScalarType);
    }

    // Number types
    final var byteType = new ScalarTypeByte();
    addType(Byte.class, byteType);
    addType(byte.class, byteType);
    nativeMap.put(Types.TINYINT, byteType);

    final var shortType = new ScalarTypeShort();
    addType(Short.class, shortType);
    addType(short.class, shortType);
    nativeMap.put(Types.SMALLINT, shortType);

    final var integerType = new ScalarTypeInteger();
    addType(Integer.class, integerType);
    addType(int.class, integerType);
    nativeMap.put(Types.INTEGER, integerType);

    final var longType = new ScalarTypeLong();
    addType(Long.class, longType);
    addType(long.class, longType);
    nativeMap.put(Types.BIGINT, longType);

    final var doubleType = new ScalarTypeDouble();
    addType(Double.class, doubleType);
    addType(double.class, doubleType);
    nativeMap.put(Types.FLOAT, doubleType);// no this is not a bug
    nativeMap.put(Types.DOUBLE, doubleType);

    final var floatType = new ScalarTypeFloat();
    addType(Float.class, floatType);
    addType(float.class, floatType);
    nativeMap.put(Types.REAL, floatType);// no this is not a bug

    final var bigDecimalType = new ScalarTypeBigDecimal();
    addType(BigDecimal.class, bigDecimalType);
    nativeMap.put(Types.DECIMAL, bigDecimalType);
    nativeMap.put(Types.NUMERIC, bigDecimalType);

    // Temporal types
    final var timeType = new ScalarTypeTime();
    addType(Time.class, timeType);
    nativeMap.put(Types.TIME, timeType);

    final var dateType = new ScalarTypeDate(jsonDate);
    addType(Date.class, dateType);
    nativeMap.put(Types.DATE, dateType);

    final var timestampType = new ScalarTypeTimestamp(jsonDateTime);
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
