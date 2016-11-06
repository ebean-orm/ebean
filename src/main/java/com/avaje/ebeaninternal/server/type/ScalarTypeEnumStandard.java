package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.text.TextException;
import com.avaje.ebeanservice.docstore.api.mapping.DocPropertyType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * JPA standard based Enum scalar type.
 * <p>
 * Converts between bean Enum types to database string or integer columns.
 * </p>
 * <p>
 * The limitation of this class is that it converts the Enum to either the ordinal or string value
 * of the Enum. If you wish to convert the Enum to some other value then you should look at the
 * Ebean specific @EnumMapping.
 * </p>
 */
public class ScalarTypeEnumStandard {

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static class StringEnum extends EnumBase implements ScalarTypeEnum {

    private final int length;

    /**
     * Create a ScalarTypeEnum.
     */
    public StringEnum(Class enumType) {
      super(enumType, false, Types.VARCHAR);
      this.length = maxValueLength(enumType);
    }

    /**
     * Return the IN values for DB constraint construction.
     */
    @Override
    public Set<String> getDbCheckConstraintValues() {

      LinkedHashSet<String> values = new LinkedHashSet<>();

      Object[] ea = enumType.getEnumConstants();
      for (int i = 0; i < ea.length; i++) {
        Enum<?> e = (Enum<?>) ea[i];
        values.add("'" + e.name() + "'");
      }
      return values;
    }

    private int maxValueLength(Class<?> enumType) {

      int maxLen = 0;

      Object[] ea = enumType.getEnumConstants();
      for (int i = 0; i < ea.length; i++) {
        Enum<?> e = (Enum<?>) ea[i];
        maxLen = Math.max(maxLen, e.name().length());
      }

      return maxLen;
    }

    public int getLength() {
      return length;
    }

    public void bind(DataBind b, Object value) throws SQLException {
      if (value == null) {
        b.setNull(Types.VARCHAR);
      } else {
        b.setString(format(value));
      }
    }

    public Object read(DataReader dataReader) throws SQLException {

      String string = dataReader.getString();
      if (string == null) {
        return null;
      } else {
        return parse(string);
      }
    }

    /**
     * Convert the Boolean value to the db value.
     */
    public Object toJdbcType(Object beanValue) {
      if (beanValue == null) {
        return null;
      }
      return format(beanValue);
    }

    public Object toBeanType(Object dbValue) {
      if (dbValue == null || dbValue instanceof Enum<?>) {
        return dbValue;
      }
      return Enum.valueOf(enumType, (String) dbValue);
    }

  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static class OrdinalEnum extends EnumBase implements ScalarTypeEnum {

    private final Object[] enumArray;

    /**
     * Create a ScalarTypeEnum.
     */
    public OrdinalEnum(Class enumType) {
      super(enumType, false, Types.INTEGER);
      this.enumArray = EnumSet.allOf(enumType).toArray();
    }

    /**
     * Return the IN values for DB constraint construction.
     */
    @Override
    public Set<String> getDbCheckConstraintValues() {

      LinkedHashSet<String> values = new LinkedHashSet<>();
      for (int i = 0; i < enumArray.length; i++) {
        Enum<?> e = (Enum<?>) enumArray[i];
        values.add(Integer.toString(e.ordinal()));
      }
      return values;
    }

    public void bind(DataBind b, Object value) throws SQLException {
      if (value == null) {
        b.setNull(Types.INTEGER);
      } else {
        b.setInt(((Enum<?>) value).ordinal());
      }
    }

    public Object read(DataReader dataReader) throws SQLException {

      Integer ordinal = dataReader.getInt();
      if (ordinal == null) {
        return null;
      } else {
        if (ordinal < 0 || ordinal >= enumArray.length) {
          String m = "Unexpected ordinal [" + ordinal + "] out of range [" + enumArray.length + "]";
          throw new IllegalStateException(m);
        }
        return enumArray[ordinal];
      }
    }

    /**
     * Convert the Enum value to the db value.
     */
    public Object toJdbcType(Object beanValue) {
      if (beanValue == null) {
        return null;
      }
      return ((Enum<?>) beanValue).ordinal();
    }

    /**
     * Convert the db value to the Enum value.
     */
    public Object toBeanType(Object dbValue) {
      if (dbValue == null || dbValue instanceof Enum<?>) {
        return dbValue;
      }

      int ordinal = (Integer) dbValue;
      if (ordinal < 0 || ordinal >= enumArray.length) {
        String m = "Unexpected ordinal [" + ordinal + "] out of range [" + enumArray.length + "]";
        throw new IllegalStateException(m);
      }
      return enumArray[ordinal];
    }

  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public abstract static class EnumBase extends ScalarTypeBase {

    protected final Class enumType;

    public EnumBase(Class<?> type, boolean jdbcNative, int jdbcType) {
      super(type, jdbcNative, jdbcType);
      this.enumType = type;
    }

    @Override
    public String format(Object value) {
      return ((Enum<?>) value).name();
    }

    @Override
    public String formatValue(Object value) {
      return ((Enum<?>) value).name();
    }

    @Override
    public Object parse(String value) {
      return Enum.valueOf(enumType, value);
    }

    @Override
    public Object convertFromMillis(long systemTimeMillis) {
      throw new TextException("Not Supported");
    }

    @Override
    public boolean isDateTimeCapable() {
      return false;
    }

    @Override
    public Object jsonRead(JsonParser parser) throws IOException {
      return parse(parser.getValueAsString());
    }

    @Override
    public void jsonWrite(JsonGenerator writer, Object value) throws IOException {
      writer.writeString(formatValue(value));
    }

    @Override
    public DocPropertyType getDocType() {
      return DocPropertyType.ENUM;
    }

    @Override
    public Object readData(DataInput dataInput) throws IOException {
      if (!dataInput.readBoolean()) {
        return null;
      } else {
        return parse(dataInput.readUTF());
      }
    }

    @Override
    public void writeData(DataOutput dataOutput, Object value) throws IOException {
      if (value == null) {
        dataOutput.writeBoolean(false);
      } else {
        ScalarHelp.writeUTF(dataOutput, format(value));
      }
    }
  }
}
