package io.ebean.core.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import io.ebean.text.StringFormatter;
import io.ebean.text.StringParser;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Describes a scalar type.
 * <p>
 * Scalar in the sense that the types are not compound types. Scalar types only
 * map to a single database column.
 * <p>
 * These types fall into two categories. Types that are mapped natively to JDBC
 * types and the rest. Types that map to native JDBC types do not require any
 * data type conversion to be persisted to the database. These are java types
 * that map via java.sql.Types.
 * <p>
 * Types that are not native to JDBC require some conversion. These include some
 * common java types such as java.util.Date, java.util.Calendar,
 * java.math.BigInteger.
 * <p>
 * Note that Booleans may be native for some databases and require conversion on
 * other databases.
 */
public interface ScalarType<T> extends StringParser, StringFormatter, ScalarDataReader<T> {

  /**
   * Return true for types that do mutation detection based on json content.
   */
  default boolean jsonMapper() {
    return false;
  }

  /**
   * Return true if this is a binary type and can not support parse() and format() from/to string.
   * This allows Ebean to optimise marshalling types to string.
   */
  default boolean binary() {
    return false;
  }

  /**
   * Return true if this is a mutable scalar type (like hstore).
   */
  default boolean mutable() {
    return false;
  }

  /**
   * For mutable scalarType's return true if the value is dirty.
   * Non-dirty properties may be excluded from updates.
   */
  default boolean isDirty(Object value) {
    return false;
  }

  /**
   * Return the default DB column length for this type.
   * <p>
   * If a BeanProperty has no explicit length defined then this length should
   * be assigned.
   */
  default int length() {
    return 0;
  }

  /**
   * Return true if the type is native to JDBC.
   * <p>
   * If it is native to JDBC then its values/instances do not need to be
   * converted to and from an associated JDBC type.
   */
  boolean jdbcNative();

  /**
   * Return the type as per java.sql.Types that this maps to.
   * <p>
   * This type should be consistent with the toJdbcType() method in converting
   * the type to the appropriate type for binding to preparedStatements.
   */
  int jdbcType();

  /**
   * Return the type that matches the bean property type.
   * <p>
   * This represents the 'logical' type rather than the JDBC type this maps to.
   */
  Class<T> type();

  /**
   * Read the value from the resultSet and convert if necessary to the logical
   * bean property value.
   */
  @Override
  T read(DataReader reader) throws SQLException;

  /**
   * Convert (if necessary) and bind the value to the preparedStatement.
   * <p>
   * value may need to be converted from the logical bean property type to the
   * JDBC type.
   */
  void bind(DataBinder binder, T value) throws SQLException;

  /**
   * Convert the value as necessary to the JDBC type.
   * <p>
   * Note that this should also match the type as per the getJdbcType() method.
   * <p>
   * This is typically used when the matching type is used in a where clause
   * and we use this to ensure it is an appropriate jdbc type.
   */
  Object toJdbcType(Object value);

  /**
   * Convert the value as necessary to the logical Bean type.
   * <p>
   * The type as per the bean property.
   * <p>
   * This is used to automatically convert id values (typically from a string
   * to a int, long or UUID).
   */
  T toBeanType(Object value);

  /**
   * Convert the type into a string representation.
   */
  String formatValue(T value);

  /**
   * Convert the type into a string representation.
   * <p>
   * This assumes the value is of the correct type.
   * <p>
   * This is so that ScalarType also implements the StringFormatter interface.
   */
  @Override
  String format(Object value);

  /**
   * Convert the string value to the appropriate java object.
   * <p>
   * Mostly used to support CSV, JSON and XML parsing.
   */
  @Override
  T parse(String value);

  /**
   * Return the type this maps to for JSON document stores.
   */
  DocPropertyType docType();

  /**
   * Convert the value into a long version value.
   */
  default long asVersion(T value) {
    throw new UnsupportedOperationException();
  }

  /**
   * Convert the systemTimeMillis into the appropriate java object.
   * <p>
   * For non dateTime types this will throw an exception.
   */
  default T convertFromMillis(long dateTime) {
    throw new UnsupportedOperationException();
  }

  /**
   * Read the value from binary input.
   */
  T readData(DataInput dataInput) throws IOException;

  /**
   * Write the value to binary output.
   */
  void writeData(DataOutput dataOutput, T value) throws IOException;

  /**
   * Read the value from JsonParser.
   */
  T jsonRead(JsonParser parser) throws IOException;

  /**
   * Write the value to the JsonGenerator.
   */
  void jsonWrite(JsonGenerator writer, T value) throws IOException;

}
