package io.ebeaninternal.server.type;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.text.json.EJson;
import io.ebeaninternal.json.ModifyAwareList;
import io.ebeanservice.docstore.api.mapping.DocPropertyType;

import javax.persistence.PersistenceException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Array;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Type mapped for DB ARRAY type (Postgres only effectively).
 */
@SuppressWarnings("rawtypes")
public class ScalarTypeArrayList extends ScalarTypeJsonCollection<List> implements ScalarTypeArray {

  private static ScalarTypeArrayList UUID = new ScalarTypeArrayList("uuid", DocPropertyType.UUID, ArrayElementConverter.UUID);
  private static ScalarTypeArrayList LONG = new ScalarTypeArrayList("bigint", DocPropertyType.LONG, ArrayElementConverter.LONG);
  private static ScalarTypeArrayList INTEGER = new ScalarTypeArrayList("integer", DocPropertyType.INTEGER, ArrayElementConverter.INTEGER);
  private static ScalarTypeArrayList DOUBLE = new ScalarTypeArrayList("float", DocPropertyType.DOUBLE, ArrayElementConverter.DOUBLE);
  private static ScalarTypeArrayList STRING = new ScalarTypeArrayList("varchar", DocPropertyType.TEXT, ArrayElementConverter.STRING);

  static PlatformArrayTypeFactory factory() {
    return new Factory();
  }

  static class Factory implements PlatformArrayTypeFactory {

    /**
     * Return the ScalarType to use based on the List's generic parameter type.
     */
    @Override
    public ScalarTypeArrayList typeFor(Type valueType) {
      if (valueType.equals(UUID.class)) {
        return UUID;
      }
      if (valueType.equals(Long.class)) {
        return LONG;
      }
      if (valueType.equals(Integer.class)) {
        return INTEGER;
      }
      if (valueType.equals(Double.class)) {
        return DOUBLE;
      }
      if (valueType.equals(String.class)) {
        return STRING;
      }
      throw new IllegalArgumentException("Type [" + valueType + "] not supported for @DbArray mapping");
    }

    @Override
    public ScalarTypeArrayList typeForEnum(ScalarType<?> scalarType) {
      final String arrayType;
      switch (scalarType.getJdbcType()) {
        case Types.INTEGER:
          arrayType = "integer";
          break;
        case Types.VARCHAR:
          arrayType = "varchar";
          break;
        default:
          throw new IllegalArgumentException("JdbcType [" + scalarType.getJdbcType() + "] not supported for @DbArray mapping on set.");
      }

      return new ScalarTypeArrayList(arrayType, scalarType.getDocType(), new ArrayElementConverter.EnumConverter(scalarType));
    }
  }

  private final String arrayType;

  private final ArrayElementConverter converter;

  public ScalarTypeArrayList(String arrayType, DocPropertyType docPropertyType, ArrayElementConverter converter) {
    super(List.class, Types.ARRAY, docPropertyType);
    this.arrayType = arrayType;
    this.converter = converter;
  }

  @Override
  public DocPropertyType getDocType() {
    return docPropertyType;
  }

  /**
   * Return the DB column definition for DDL generation.
   */
  @Override
  public String getDbColumnDefn() {
    return arrayType + "[]";
  }

  @SuppressWarnings("unchecked")
  private List fromArray(Object[] array1) {
    List list = new ArrayList(array1.length);
    for (Object element : array1) {
      list.add(converter.toElement(element));
    }
    return new ModifyAwareList(list);
  }

  protected Object[] toArray(List value) {
    return converter.toDbArray(value.toArray());
  }

  @Override
  public List read(DataReader reader) throws SQLException {
    Array array = reader.getArray();
    if (array == null) {
      return null;
    } else {
      return fromArray((Object[]) array.getArray());
    }
  }

  @Override
  public void bind(DataBind bind, List value) throws SQLException {
    if (value == null) {
      bind.setNull(Types.ARRAY);
    } else {
      bind.setArray(arrayType, toArray(value));
    }
  }

  @Override
  public String formatValue(List value) {
    try {
      return EJson.write(value);
    } catch (IOException e) {
      throw new PersistenceException("Failed to format List into JSON content", e);
    }
  }

  @Override
  public List parse(String value) {
    try {
      return EJson.parseList(value, false);
    } catch (IOException e) {
      throw new PersistenceException("Failed to parse JSON content as List: [" + value + "]", e);
    }
  }

  @Override
  public List jsonRead(JsonParser parser) throws IOException {
    return EJson.parseList(parser, parser.getCurrentToken());
  }

  @Override
  public void jsonWrite(JsonGenerator writer, List value) throws IOException {
    EJson.write(value, writer);
  }

}
