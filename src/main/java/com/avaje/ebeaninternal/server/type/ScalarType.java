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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;

import com.avaje.ebean.text.StringFormatter;
import com.avaje.ebean.text.StringParser;
import com.avaje.ebean.text.json.JsonValueAdapter;
import com.avaje.ebeaninternal.server.text.json.WriteJsonBuffer;

/**
 * Describes a scalar type.
 * <p>
 * Scalar in the sense that the types are not compound types. Scalar types only
 * map to a single database column.
 * </p>
 * <p>
 * These types fall into two categories. Types that are mapped natively to JDBC
 * types and the rest. Types that map to native JDBC types do not require any
 * data type conversion to be persisted to the database. These are java types
 * that map via java.sql.Types.
 * </p>
 * <p>
 * Types that are not native to JDBC require some conversion. These include some
 * common java types such as java.util.Date, java.util.Calendar,
 * java.math.BigInteger.
 * </p>
 * <p>
 * Note that Booleans may be native for some databases and require conversion on
 * other databases.
 * </p>
 */
public interface ScalarType<T> extends StringParser, StringFormatter, ScalarDataReader<T> {

	/**
	 * Return the default DB column length for this type.
	 * <p>
	 * If a BeanProperty has no explicit length defined then this length should
	 * be assigned.
	 * </p>
	 * <p>
	 * This is primarily to support defining a length on Enum types (to
	 * supplement defining the length on the BeanProperty directly).
	 * </p>
	 */
	public int getLength();

	/**
	 * Return true if the type is native to JDBC.
	 * <p>
	 * If it is native to JDBC then its values/instances do not need to be
	 * converted to and from an associated JDBC type.
	 * </p>
	 */
	public boolean isJdbcNative();

	/**
	 * Return the type as per java.sql.Types that this maps to.
	 * <p>
	 * This type should be consistent with the toJdbcType() method in converting
	 * the type to the appropriate type for binding to preparedStatements.
	 * </p>
	 */
	public int getJdbcType();

	/**
	 * Return the type that matches the bean property type.
	 * <p>
	 * This represents the 'logical' type rather than the JDBC type this maps
	 * to.
	 * </p>
	 */
	public Class<T> getType();

	/**
	 * Read the value from the resultSet and convert if necessary to the logical
	 * bean property value.
	 */
    public T read(DataReader dataReader) throws SQLException;

    /**
     * Ignore the reading of this value. Typically this means moving the index
     * position in the ResultSet.
     */
    public void loadIgnore(DataReader dataReader);

	/**
	 * Convert (if necessary) and bind the value to the preparedStatement.
	 * <p>
	 * value may need to be converted from the logical bean property type to the
	 * JDBC type.
	 * </p>
	 */
	public void bind(DataBind b, T value) throws SQLException;

	/**
	 * Convert the value as necessary to the JDBC type.
	 * <p>
	 * Note that this should also match the type as per the getJdbcType()
	 * method.
	 * </p>
	 * <p>
	 * This is typically used when the matching type is used in a where clause
	 * and we use this to ensure it is an appropriate jdbc type.
	 * </p>
	 */
	public Object toJdbcType(Object value);

	/**
	 * Convert the value as necessary to the logical Bean type.
	 * <p>
	 * The type as per the bean property.
	 * </p>
	 * <p>
	 * This is used to automatically convert id values (typically from a string
	 * to a int, long or UUID).
	 * </p>
	 */
	public T toBeanType(Object value);

	/**
	 * Convert the type into a string representation.
	 * <p>
	 * Reciprocal of parse().
	 * </p>
	 */
	public String formatValue(T v);

    /**
     * Convert the type into a string representation.
     * <p>
     * This assumes the value is of the correct type.
     * </p>
     * <p>
     * This is so that ScalarType also implements the StringFormatter interface.
     * </p>
     */
	public String format(Object v);

	/**
	 * Convert the string value to the appropriate java object.
	 * <p>
	 * Mostly used to support CSV, JSON and XML parsing.
	 * </p>
     * <p>
     * Reciprocal of formatValue().
     * </p>
	 */
	public T parse(String value);

	/**
	 * Convert the systemTimeMillis into the appropriate java object.
	 * <p>
	 * For non dateTime types this will throw an exception.
	 * </p>
	 */
	public T parseDateTime(long dateTime);

	/**
	 * Return true if the type can accept long systemTimeMillis input.
	 * <p>
	 * This is used to determine if is is sensible to use the
	 * {@link #parseDateTime(long)} method.
	 * </p>
	 * <p>
	 * This includes the Date, Calendar, sql Date, Time, Timestamp, JODA types
	 * as well as Long, BigDecimal and String (although it generally is not
	 * expected to parse systemTimeMillis to a String or BigDecimal).
	 * </p>
	 */
	public boolean isDateTimeCapable();

	public void jsonWrite(WriteJsonBuffer buffer, T value, JsonValueAdapter ctx);
	
	public String jsonToString(T value, JsonValueAdapter ctx);

    public T jsonFromString(String value, JsonValueAdapter ctx);

    public Object readData(DataInput dataInput) throws IOException;

    public void writeData(DataOutput dataOutput, Object v) throws IOException;
    
}
