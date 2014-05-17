package com.avaje.ebeaninternal.server.type;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Currency;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import com.avaje.ebean.annotation.EnumMapping;
import com.avaje.ebean.annotation.EnumValue;
import com.avaje.ebean.config.CompoundType;
import com.avaje.ebean.config.CompoundTypeProperty;
import com.avaje.ebean.config.ScalarTypeConverter;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.api.ClassUtil;
import com.avaje.ebeaninternal.server.core.BootupClasses;
import com.avaje.ebeaninternal.server.lib.util.StringHelper;
import com.avaje.ebeaninternal.server.type.reflect.CheckImmutable;
import com.avaje.ebeaninternal.server.type.reflect.CheckImmutableResponse;
import com.avaje.ebeaninternal.server.type.reflect.ImmutableMeta;
import com.avaje.ebeaninternal.server.type.reflect.ImmutableMetaFactory;
import com.avaje.ebeaninternal.server.type.reflect.KnownImmutable;
import com.avaje.ebeaninternal.server.type.reflect.ReflectionBasedCompoundType;
import com.avaje.ebeaninternal.server.type.reflect.ReflectionBasedCompoundTypeProperty;
import com.avaje.ebeaninternal.server.type.reflect.ReflectionBasedTypeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of TypeManager.
 * <p>
 * Manages the list of ScalarType that is available.
 * </p>
 */
public final class DefaultTypeManager implements TypeManager, KnownImmutable {

  private static final Logger logger = LoggerFactory.getLogger(DefaultTypeManager.class);

  private final ConcurrentHashMap<Class<?>, CtCompoundType<?>> compoundTypeMap;

  private final ConcurrentHashMap<Class<?>, ScalarType<?>> typeMap;

  private final ConcurrentHashMap<Integer, ScalarType<?>> nativeMap;

  private final DefaultTypeFactory extraTypeFactory;

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

  private final ScalarType<?> dateType = new ScalarTypeDate();

  private final ScalarType<?> timestampType = new ScalarTypeTimestamp();

  private final ScalarType<?> urlType = new ScalarTypeURL();
  private final ScalarType<?> uriType = new ScalarTypeURI();
  private final ScalarType<?> localeType = new ScalarTypeLocale();
  private final ScalarType<?> currencyType = new ScalarTypeCurrency();
  private final ScalarType<?> timeZoneType = new ScalarTypeTimeZone();

  private final ScalarType<?> stringType = new ScalarTypeString();

  private final ScalarType<?> classType = new ScalarTypeClass();

  private final ScalarTypeLongToTimestamp longToTimestamp = new ScalarTypeLongToTimestamp();

  private final List<ScalarType<?>> customScalarTypes = new ArrayList<ScalarType<?>>();

  private final CheckImmutable checkImmutable;

  private final ImmutableMetaFactory immutableMetaFactory = new ImmutableMetaFactory();

  private final ReflectionBasedTypeBuilder reflectScalarBuilder;

  /**
   * Create the DefaultTypeManager.
   */
  public DefaultTypeManager(ServerConfig config, BootupClasses bootupClasses) {

    int clobType = config == null ? Types.CLOB : config.getDatabasePlatform().getClobDbType();
    int blobType = config == null ? Types.BLOB : config.getDatabasePlatform().getBlobDbType();

    this.checkImmutable = new CheckImmutable(this);
    this.reflectScalarBuilder = new ReflectionBasedTypeBuilder(this);

    this.compoundTypeMap = new ConcurrentHashMap<Class<?>, CtCompoundType<?>>();
    this.typeMap = new ConcurrentHashMap<Class<?>, ScalarType<?>>();
    this.nativeMap = new ConcurrentHashMap<Integer, ScalarType<?>>();

    this.extraTypeFactory = new DefaultTypeFactory(config);

    initialiseStandard(clobType, blobType, config.isUuidStoreAsBinary());
    initialiseJodaTypes();

    if (bootupClasses != null) {
      initialiseCustomScalarTypes(bootupClasses);
      initialiseScalarConverters(bootupClasses);
      initialiseCompoundTypes(bootupClasses);
    }
  }

  public boolean isKnownImmutable(Class<?> cls) {

    if (cls == null) {
      // superclass from an interface
      return true;
    }

    if (cls.isPrimitive() || Object.class.equals(cls)) {
      return true;
    }

    ScalarDataReader<?> scalarDataReader = getScalarDataReader(cls);
    return scalarDataReader != null;
  }

  public CheckImmutableResponse checkImmutable(Class<?> cls) {
    return checkImmutable.checkImmutable(cls);
  }

  private ScalarType<?> register(ScalarType<?> st) {
    add(st);
    logger.debug("Registering ScalarType for " + st.getType() + " implemented using reflection");
    return st;
  }

  public ScalarDataReader<?> recursiveCreateScalarDataReader(Class<?> cls) {

    ScalarDataReader<?> scalarReader = getScalarDataReader(cls);
    if (scalarReader != null) {
      return scalarReader;
    }

    ImmutableMeta meta = immutableMetaFactory.createImmutableMeta(cls);

    if (!meta.isCompoundType()) {
      return register(reflectScalarBuilder.buildScalarType(meta));
    }

    ReflectionBasedCompoundType compoundType = reflectScalarBuilder.buildCompound(meta);
    Class<?> compoundTypeClass = compoundType.getCompoundType();

    return createCompoundScalarDataReader(compoundTypeClass, compoundType, " using reflection");

  }

  public ScalarType<?> recursiveCreateScalarTypes(Class<?> cls) {

    ScalarType<?> scalarType = getScalarType(cls);
    if (scalarType != null) {
      return scalarType;
    }

    ImmutableMeta meta = immutableMetaFactory.createImmutableMeta(cls);

    if (!meta.isCompoundType()) {
      return register(reflectScalarBuilder.buildScalarType(meta));
    }

    throw new RuntimeException("Not allowed compound types here");

  }

  /**
   * Register a custom ScalarType.
   */
  public void add(ScalarType<?> scalarType) {
    typeMap.put(scalarType.getType(), scalarType);
    logAdd(scalarType);
  }

  protected void logAdd(ScalarType<?> scalarType) {
    if (logger.isDebugEnabled()) {
      String msg = "ScalarType register [" + scalarType.getClass().getName() + "]";
      msg += " for [" + scalarType.getType().getName() + "]";
      logger.debug(msg);
    }
  }

  public CtCompoundType<?> getCompoundType(Class<?> type) {
    return compoundTypeMap.get(type);
  }

  /**
   * Return the ScalarType for the given jdbc type as per java.sql.Types.
   */
  public ScalarType<?> getScalarType(int jdbcType) {
    return nativeMap.get(jdbcType);
  }

  /**
   * This can return null if no matching ScalarType is found.
   */
  @SuppressWarnings("unchecked")
  public <T> ScalarType<T> getScalarType(Class<T> type) {
    return (ScalarType<T>) typeMap.get(type);
  }

  public ScalarDataReader<?> getScalarDataReader(Class<?> propertyType, int sqlType) {

    if (sqlType == 0) {
      return recursiveCreateScalarDataReader(propertyType);
    }

    for (int i = 0; i < customScalarTypes.size(); i++) {
      ScalarType<?> customScalarType = customScalarTypes.get(i);

      if (sqlType == customScalarType.getJdbcType() && (propertyType.equals(customScalarType.getType()))) {

        return customScalarType;
      }
    }

    String msg = "Unable to find a custom ScalarType with type [" + propertyType + "] and java.sql.Type [" + sqlType + "]";
    throw new RuntimeException(msg);
  }

  public ScalarDataReader<?> getScalarDataReader(Class<?> type) {
    ScalarDataReader<?> reader = typeMap.get(type);
    if (reader == null) {
      reader = compoundTypeMap.get(type);
    }
    return reader;
  }

  /**
   * Return a ScalarType for a given class.
   * <p>
   * Used for java.util.Date and java.util.Calendar which can be mapped to
   * different jdbcTypes in a single system.
   * </p>
   */
  @SuppressWarnings("unchecked")
  public <T> ScalarType<T> getScalarType(Class<T> type, int jdbcType) {

    // check for Clob, LongVarchar etc first...
    // the reason being that String maps to multiple jdbc types
    // varchar, clob, longVarchar.
    ScalarType<?> scalarType = getLobTypes(jdbcType);
    if (scalarType != null) {
      // it is a specific Lob type...
      return (ScalarType<T>) scalarType;
    }

    scalarType = typeMap.get(type);
    if (scalarType != null) {
      if (jdbcType == 0 || scalarType.getJdbcType() == jdbcType) {
        // matching type
        return (ScalarType<T>) scalarType;
      } else {
        // sometime like java.util.Date or java.util.Calendar
        // that that does not map to the same jdbc type as the
        // server wide settings.
      }
    }
    // a util Date with jdbcType not matching server wide settings
    if (type.equals(java.util.Date.class)) {
      return (ScalarType<T>) extraTypeFactory.createUtilDate(jdbcType);
    }
    // a Calendar with jdbcType not matching server wide settings
    if (type.equals(java.util.Calendar.class)) {
      return (ScalarType<T>) extraTypeFactory.createCalendar(jdbcType);
    }

    String msg = "Unmatched ScalarType for " + type + " jdbcType:" + jdbcType;
    throw new RuntimeException(msg);
  }

  /**
   * Return the types for the known lob types.
   * <p>
   * Kind of special case because these map multiple jdbc types to single Java
   * types - like String - Varchar, LongVarchar, Clob. For this reason I check
   * for the specific Lob types first before looking for a matching type.
   * </p>
   */
  private ScalarType<?> getLobTypes(int jdbcType) {

    return getScalarType(jdbcType);
  }

  /**
   * Convert the Object to the required datatype. The
   * 
   * @param value
   *          the Object value
   * @param toJdbcType
   *          the type as per java.sql.Types.
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

  private boolean isIntegerType(String s) {

    try {
      Integer.parseInt(s);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * Create the Mapping of Enum fields to DB values using EnumValue annotations.
   * <p>
   * Return null if the EnumValue annotations are not present/used.
   * </p>
   */
  private ScalarType<?> createEnumScalarType2(Class<?> enumType) {

    boolean integerType = true;

    Map<String, String> nameValueMap = new HashMap<String, String>();

    Field[] fields = enumType.getDeclaredFields();
    for (int i = 0; i < fields.length; i++) {
      EnumValue enumValue = fields[i].getAnnotation(EnumValue.class);
      if (enumValue != null) {
        nameValueMap.put(fields[i].getName(), enumValue.value());
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

    return createEnumScalarType(enumType, nameValueMap, integerType, 0);
  }
    
  /**
   * Create a ScalarType for an Enum that has additional mapping.
   * <p>
   * The reason for this is that often in a DB there will be short codes used
   * such as A,I,N rather than the ACTIVE, INACTIVE, NEW. So there really needs
   * to be a mapping from the nicely named enumeration values to the typically
   * much shorter codes used in the DB.
   * </p>
   */
  public ScalarType<?> createEnumScalarType(Class<?> enumType) {

    // get the mapping information from EnumMapping
    EnumMapping enumMapping = (EnumMapping) enumType.getAnnotation(EnumMapping.class);
    if (enumMapping == null) {
      // look for EnumValue annotations instead
      return createEnumScalarType2(enumType);
    }

    String nameValuePairs = enumMapping.nameValuePairs();
    boolean integerType = enumMapping.integerType();
    int dbColumnLength = enumMapping.length();

    Map<String, String> nameValueMap = StringHelper.delimitedToMap(nameValuePairs, ",", "=");

    return createEnumScalarType(enumType, nameValueMap, integerType, dbColumnLength);
  }

  /**
   * Given the name value mapping and integer/string type and explicit DB column
   * length create the ScalarType for the Enum.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private ScalarType<?> createEnumScalarType(Class enumType, Map<String, String> nameValueMap, boolean integerType, int dbColumnLength) {

    EnumToDbValueMap<?> beanDbMap = EnumToDbValueMap.create(integerType);

    int maxValueLen = 0;

    Iterator it = nameValueMap.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry entry = (Map.Entry) it.next();
      String name = (String) entry.getKey();
      String value = (String) entry.getValue();

      maxValueLen = Math.max(maxValueLen, value.length());

      Object enumValue = Enum.valueOf(enumType, name.trim());
      beanDbMap.add(enumValue, value.trim());
    }

    if (dbColumnLength == 0 && !integerType) {
      dbColumnLength = maxValueLen;
    }

    return new ScalarTypeEnumWithMapping(beanDbMap, enumType, dbColumnLength);
  }

  /**
   * Automatically find any ScalarTypes by searching through the class path.
   * <p>
   * In avaje.properties define a list of packages in which ScalarTypes are
   * found. This will search for any class that implements the ScalarType
   * interface and register it with this TypeManager.
   * </p>
   */
  protected void initialiseCustomScalarTypes(BootupClasses bootupClasses) {

    customScalarTypes.add(longToTimestamp);

    List<Class<?>> foundTypes = bootupClasses.getScalarTypes();

    for (int i = 0; i < foundTypes.size(); i++) {
      Class<?> cls = foundTypes.get(i);
      try {

        ScalarType<?> scalarType = (ScalarType<?>) cls.newInstance();
        add(scalarType);

        customScalarTypes.add(scalarType);

      } catch (Exception e) {
        String msg = "Error loading ScalarType [" + cls.getName() + "]";
        logger.error(msg, e);
      }
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected void initialiseScalarConverters(BootupClasses bootupClasses) {

    List<Class<?>> foundTypes = bootupClasses.getScalarConverters();

    for (int i = 0; i < foundTypes.size(); i++) {
      Class<?> cls = foundTypes.get(i);
      try {

        Class<?>[] paramTypes = TypeReflectHelper.getParams(cls, ScalarTypeConverter.class);
        if (paramTypes.length != 2) {
          throw new IllegalStateException("Expected 2 generics paramtypes but got: " + Arrays.toString(paramTypes));
        }

        Class<?> logicalType = paramTypes[0];
        Class<?> persistType = paramTypes[1];

        ScalarType<?> wrappedType = getScalarType(persistType);
        if (wrappedType == null) {
          throw new IllegalStateException("Could not find ScalarType for: " + paramTypes[1]);
        }

        ScalarTypeConverter converter = (ScalarTypeConverter) cls.newInstance();
        ScalarTypeWrapper stw = new ScalarTypeWrapper(logicalType, wrappedType, converter);

        logger.debug("Register ScalarTypeWrapper from " + logicalType + " -> " + persistType + " using:" + cls);

        add(stw);

      } catch (Exception e) {
        String msg = "Error loading ScalarType [" + cls.getName() + "]";
        logger.error(msg, e);
      }
    }

  }

  protected void initialiseCompoundTypes(BootupClasses bootupClasses) {

    ArrayList<Class<?>> compoundTypes = bootupClasses.getCompoundTypes();
    for (int j = 0; j < compoundTypes.size(); j++) {

      Class<?> type = compoundTypes.get(j);
      try {

        Class<?>[] paramTypes = TypeReflectHelper.getParams(type, CompoundType.class);
        if (paramTypes.length != 1) {
          throw new RuntimeException("Expecting 1 generic paramter type but got " + Arrays.toString(paramTypes) + " for " + type);
        }

        Class<?> compoundTypeClass = paramTypes[0];

        CompoundType<?> compoundType = (CompoundType<?>) type.newInstance();
        createCompoundScalarDataReader(compoundTypeClass, compoundType, "");

      } catch (Exception e) {
        String msg = "Error initialising component " + type;
        throw new RuntimeException(msg, e);
      }
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected CtCompoundType createCompoundScalarDataReader(Class<?> compoundTypeClass, CompoundType<?> compoundType, String info) {

    CtCompoundType<?> ctCompoundType = compoundTypeMap.get(compoundTypeClass);
    if (ctCompoundType != null) {
      logger.info("Already registered compound type " + compoundTypeClass);
      return ctCompoundType;
    }

    CompoundTypeProperty<?, ?>[] cprops = compoundType.getProperties();

    ScalarDataReader[] dataReaders = new ScalarDataReader[cprops.length];

    for (int i = 0; i < cprops.length; i++) {

      Class<?> propertyType = getCompoundPropertyType(cprops[i]);

      ScalarDataReader<?> scalarDataReader = getScalarDataReader(propertyType, cprops[i].getDbType());
      if (scalarDataReader == null) {
        throw new RuntimeException("Could not find ScalarDataReader for " + propertyType);
      }

      dataReaders[i] = scalarDataReader;
    }

    CtCompoundType ctType = new CtCompoundType(compoundTypeClass, compoundType, dataReaders);

    logger.debug("Registering CompoundType " + compoundTypeClass + " " + info);
    compoundTypeMap.put(compoundTypeClass, ctType);

    return ctType;
  }

  /**
   * Return the property type for a given property of a compound type.
   */
  private Class<?> getCompoundPropertyType(CompoundTypeProperty<?, ?> prop) {

    if (prop instanceof ReflectionBasedCompoundTypeProperty) {
      return ((ReflectionBasedCompoundTypeProperty) prop).getPropertyType();
    }

    // determine the types from generic parameter types using reflection
    Class<?>[] propParamTypes = TypeReflectHelper.getParams(prop.getClass(), CompoundTypeProperty.class);
    if (propParamTypes.length != 2) {
      throw new RuntimeException("Expecting 2 generic paramter types but got " + Arrays.toString(propParamTypes) + " for "
          + prop.getClass());
    }

    return propParamTypes[1];
  }

  /**
   * Detect if Joda classes are in the classpath and if so register the Joda
   * data types.
   */
  protected void initialiseJodaTypes() {

    // detect if Joda classes are in the classpath
    if (ClassUtil.isPresent("org.joda.time.LocalDateTime", this.getClass())) {
      // Joda classes are in the classpath so register the types
      logger.debug("Registering Joda data types");
      typeMap.put(LocalDateTime.class, new ScalarTypeJodaLocalDateTime());
      typeMap.put(LocalDate.class, new ScalarTypeJodaLocalDate());
      typeMap.put(LocalTime.class, new ScalarTypeJodaLocalTime());
      typeMap.put(DateTime.class, new ScalarTypeJodaDateTime());
      typeMap.put(DateMidnight.class, new ScalarTypeJodaDateMidnight());
    }
  }
	
  /**
   * Register all the standard types supported. This is the standard JDBC types
   * plus some other common types such as java.util.Date and java.util.Calendar.
   */
  protected void initialiseStandard(int platformClobType, int platformBlobType, boolean binaryUUID) {

    ScalarType<?> utilDateType = extraTypeFactory.createUtilDate();
    typeMap.put(java.util.Date.class, utilDateType);

    ScalarType<?> calType = extraTypeFactory.createCalendar();
    typeMap.put(Calendar.class, calType);

    ScalarType<?> mathBigIntType = extraTypeFactory.createMathBigInteger();
    typeMap.put(BigInteger.class, mathBigIntType);

    ScalarType<?> booleanType = extraTypeFactory.createBoolean();
    typeMap.put(Boolean.class, booleanType);
    typeMap.put(boolean.class, booleanType);

    // always register Types.BOOLEAN to our boolean type
    nativeMap.put(Types.BOOLEAN, booleanType);
    if (booleanType.getJdbcType() == Types.BIT) {
      // for MapBeans ... BIT types are assumed to be booleans
      nativeMap.put(Types.BIT, booleanType);
    } else {
      // boolean mapping to Types.Integer, Types.VARCHAR or Types.Boolean
    }

    // Store UUID as binary(16) or varchar(40)
    ScalarType<?> uuidType = (binaryUUID) ? new ScalarTypeUUIDBinary() : new ScalarTypeUUIDVarchar();
    typeMap.put(UUID.class, uuidType);

    typeMap.put(Locale.class, localeType);
    typeMap.put(Currency.class, currencyType);
    typeMap.put(TimeZone.class, timeZoneType);
    typeMap.put(URL.class, urlType);
    typeMap.put(URI.class, uriType);

    // String types
    typeMap.put(char[].class, charArrayType);
    typeMap.put(char.class, charType);
    typeMap.put(String.class, stringType);
    nativeMap.put(Types.VARCHAR, stringType);
    nativeMap.put(Types.CHAR, stringType);
    nativeMap.put(Types.LONGVARCHAR, longVarcharType);

    // Class<?>
    typeMap.put(Class.class, classType);

    if (platformClobType == Types.CLOB) {
      nativeMap.put(Types.CLOB, clobType);
    } else {
      // for Postgres Clobs handled by Varchar ScalarType...
      ScalarType<?> platClobScalarType = nativeMap.get(Integer.valueOf(platformClobType));
      if (platClobScalarType == null) {
        throw new IllegalArgumentException("Type for dbPlatform clobType [" + clobType + "] not found.");
      }
      nativeMap.put(Types.CLOB, platClobScalarType);
    }

    // Binary type
    typeMap.put(byte[].class, varbinaryType);
    nativeMap.put(Types.BINARY, binaryType);
    nativeMap.put(Types.VARBINARY, varbinaryType);
    nativeMap.put(Types.LONGVARBINARY, longVarbinaryType);

    if (platformBlobType == Types.BLOB) {
      nativeMap.put(Types.BLOB, blobType);
    } else {
      // for Postgres Blobs handled by LongVarbinary ScalarType...
      ScalarType<?> platBlobScalarType = nativeMap.get(Integer.valueOf(platformBlobType));
      if (platBlobScalarType == null) {
        throw new IllegalArgumentException("Type for dbPlatform blobType [" + blobType + "] not found.");
      }
      nativeMap.put(Types.BLOB, platBlobScalarType);
    }

    // Number types
    typeMap.put(Byte.class, byteType);
    typeMap.put(byte.class, byteType);
    nativeMap.put(Types.TINYINT, byteType);

    typeMap.put(Short.class, shortType);
    typeMap.put(short.class, shortType);
    nativeMap.put(Types.SMALLINT, shortType);

    typeMap.put(Integer.class, integerType);
    typeMap.put(int.class, integerType);
    nativeMap.put(Types.INTEGER, integerType);

    typeMap.put(Long.class, longType);
    typeMap.put(long.class, longType);
    nativeMap.put(Types.BIGINT, longType);

    typeMap.put(Double.class, doubleType);
    typeMap.put(double.class, doubleType);
    nativeMap.put(Types.FLOAT, doubleType);// no this is not a bug
    nativeMap.put(Types.DOUBLE, doubleType);

    typeMap.put(Float.class, floatType);
    typeMap.put(float.class, floatType);
    nativeMap.put(Types.REAL, floatType);// no this is not a bug

    typeMap.put(BigDecimal.class, bigDecimalType);
    nativeMap.put(Types.DECIMAL, bigDecimalType);
    nativeMap.put(Types.NUMERIC, bigDecimalType);

    // Temporal types
    typeMap.put(Time.class, timeType);
    nativeMap.put(Types.TIME, timeType);
    typeMap.put(Date.class, dateType);
    nativeMap.put(Types.DATE, dateType);
    typeMap.put(Timestamp.class, timestampType);
    nativeMap.put(Types.TIMESTAMP, timestampType);

  }

}
