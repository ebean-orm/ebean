package io.ebeaninternal.server.type;

import io.ebeanservice.docstore.api.mapping.DocPropertyType;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

abstract class ScalarTypeJsonCollection<T> extends ScalarTypeBaseMutable<T> implements ScalarTypeArray {

  protected DocPropertyType docPropertyType;

  public ScalarTypeJsonCollection(Class<T> type, int dbType, DocPropertyType docPropertyType) {
    super(type, false, dbType);
    this.docPropertyType = docPropertyType;
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
