package com.avaje.ebeaninternal.server.type;

import java.math.BigInteger;
import java.sql.Types;
import java.util.Calendar;

import com.avaje.ebean.config.JsonConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

/**
 * Helper to create some default ScalarType objects for Booleans,
 * java.util.Date, java.util.Calendar etc.
 */
public class DefaultTypeFactory {

    private final ServerConfig serverConfig;

    public DefaultTypeFactory(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    private ScalarType<Boolean> createBoolean(String trueValue, String falseValue) {

        try {
            // first try Integer based boolean
            Integer intTrue = BasicTypeConverter.toInteger(trueValue);
            Integer intFalse = BasicTypeConverter.toInteger(falseValue);

            return new ScalarTypeBoolean.IntBoolean(intTrue, intFalse);

        } catch (NumberFormatException e) {
        }

        // treat as Varchar/String based boolean
        return new ScalarTypeBoolean.StringBoolean(trueValue, falseValue);

    }

    /**
     * Create the ScalarType for mapping Booleans. For some databases this is a
     * native data type and for others Booleans will be converted to Y/N or 0/1
     * etc.
     */
    public ScalarType<Boolean> createBoolean() {

        if (serverConfig == null) {
            return new ScalarTypeBoolean.Native();
        }
        String trueValue = serverConfig.getDatabaseBooleanTrue();
        String falseValue = serverConfig.getDatabaseBooleanFalse();

        if (falseValue != null && trueValue != null) {
            // explicit integer or string based booleans
            return createBoolean(trueValue, falseValue);
        }

        // determine based on database platform configuration
        int booleanDbType = serverConfig.getDatabasePlatform().getBooleanDbType();

        // Some dbs use BIT e.g. MySQL
        if (booleanDbType == Types.BIT) {
            return new ScalarTypeBoolean.BitBoolean();
        }

        if (booleanDbType == Types.INTEGER) {
            return new ScalarTypeBoolean.IntBoolean(1, 0);
        }
        if (booleanDbType == Types.VARCHAR) {
            return new ScalarTypeBoolean.StringBoolean("T", "F");
        }

        if (booleanDbType == Types.BOOLEAN) {
            return new ScalarTypeBoolean.Native();
        }

        // assume the JDBC driver can convert the type
        return new ScalarTypeBoolean.Native();
    }

    /**
     * Create the default ScalarType for java.util.Date.
     */
    public ScalarType<java.util.Date> createUtilDate(JsonConfig.DateTime mode) {
        // by default map anonymous java.util.Date to java.sql.Timestamp.
        // String mapType =
        // properties.getProperty("type.mapping.java.util.Date","timestamp");
        int utilDateType = getTemporalMapType("timestamp");

        return createUtilDate(mode, utilDateType);
    }

    /**
     * Create a ScalarType for java.util.Date explicitly specifying the type to
     * map to.
     */
    public ScalarType<java.util.Date> createUtilDate(JsonConfig.DateTime mode, int utilDateType) {

        switch (utilDateType) {
        case Types.DATE:
            return new ScalarTypeUtilDate.DateType();

        case Types.TIMESTAMP:
            return new ScalarTypeUtilDate.TimestampType(mode);

        default:
            throw new RuntimeException("Invalid type " + utilDateType);
        }
    }

    /**
     * Create the default ScalarType for java.util.Calendar.
     */
    public ScalarType<Calendar> createCalendar(JsonConfig.DateTime mode) {

        int jdbcType = getTemporalMapType("timestamp");
        return createCalendar(mode, jdbcType);
    }

    /**
     * Create a ScalarType for java.util.Calendar explicitly specifying the type
     * to map to.
     */
    public ScalarType<Calendar> createCalendar(JsonConfig.DateTime mode, int jdbcType) {

        return new ScalarTypeCalendar(mode, jdbcType);
    }

    private int getTemporalMapType(String mapType) {
        if (mapType.equalsIgnoreCase("date")) {
            return java.sql.Types.DATE;
        }
        return java.sql.Types.TIMESTAMP;
    }

    /**
     * Create a ScalarType for java.math.BigInteger.
     */
    public ScalarType<BigInteger> createMathBigInteger() {

        return new ScalarTypeMathBigInteger();
    }
}
