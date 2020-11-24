package io.ebeaninternal.server.type;

import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DocPropertyType;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

abstract class ScalarTypeJsonCollection<T> extends ScalarTypeBase<T> implements ScalarTypeArray {

  protected final DocPropertyType docPropertyType;
  protected final boolean nullable;

  public ScalarTypeJsonCollection(Class<T> type, int dbType, DocPropertyType docPropertyType, boolean nullable) {
    super(type, false, dbType);
    this.docPropertyType = docPropertyType;
    this.nullable = nullable;
  }

  /**
   * Bind null or empty list (when not nullable).
   */
  protected void bindNull(DataBinder binder) throws SQLException {
    if (nullable) {
      binder.setNull(Types.VARCHAR);
    } else {
      binder.setString("[]");
    }
  }

  /**
   * Return the logical db column definition based on the element document type.
   */
  @Override
  public String getDbColumnDefn() {
    switch (docPropertyType) {
      case SHORT:
      case INTEGER:
      case LONG:
        return "integer[]";
      case DOUBLE:
      case FLOAT:
        return "decimal[]";
    }
    return "varchar[]";
  }

  /**
   * Consider as a mutable type. Use the isDirty() method to check for dirty state.
   */
  @Override
  public boolean isMutable() {
    return true;
  }

  /**
   * Return true if the value should be considered dirty (and included in an update).
   */
  @Override
  public boolean isDirty(Object value) {
    return CheckMarkedDirty.isDirty(value);
  }

  @Override
  public Object toJdbcType(Object value) {
    return value;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T toBeanType(Object value) {
    return (T) value;
  }

  @Override
  public DocPropertyType getDocType() {
    return docPropertyType;
  }

  @Override
  public boolean isDateTimeCapable() {
    return false;
  }

  @Override
  public T convertFromMillis(long dateTime) {
    return null;
  }

  @Override
  public T readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      return parse(dataInput.readUTF());
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, T value) throws IOException {
    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      ScalarHelp.writeUTF(dataOutput, format(value));
    }
  }

}
