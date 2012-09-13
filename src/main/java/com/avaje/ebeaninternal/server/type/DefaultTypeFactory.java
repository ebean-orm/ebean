/**
 * Copyright (C) 2006  Robin Bygrave
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
package com.avaje.ebeaninternal.server.type;

import java.math.BigInteger;
import java.sql.Types;
import java.util.Calendar;

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
    public ScalarType<java.util.Date> createUtilDate() {
        // by default map anonymous java.util.Date to java.sql.Timestamp.
        // String mapType =
        // properties.getProperty("type.mapping.java.util.Date","timestamp");
        int utilDateType = getTemporalMapType("timestamp");

        return createUtilDate(utilDateType);
    }

    /**
     * Create a ScalarType for java.util.Date explicitly specifying the type to
     * map to.
     */
    public ScalarType<java.util.Date> createUtilDate(int utilDateType) {

        switch (utilDateType) {
        case Types.DATE:
            return new ScalarTypeUtilDate.DateType();

        case Types.TIMESTAMP:
            return new ScalarTypeUtilDate.TimestampType();

        default:
            throw new RuntimeException("Invalid type " + utilDateType);
        }
    }

    /**
     * Create the default ScalarType for java.util.Calendar.
     */
    public ScalarType<Calendar> createCalendar() {
        // by default map anonymous java.util.Calendar to java.sql.Timestamp.
        // String mapType =
        // properties.getProperty("type.mapping.java.util.Calendar",
        // "timestamp");
        int jdbcType = getTemporalMapType("timestamp");

        return createCalendar(jdbcType);
    }

    /**
     * Create a ScalarType for java.util.Calendar explicitly specifying the type
     * to map to.
     */
    public ScalarType<Calendar> createCalendar(int jdbcType) {

        return new ScalarTypeCalendar(jdbcType);
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
