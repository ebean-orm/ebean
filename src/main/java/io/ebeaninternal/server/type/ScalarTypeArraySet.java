package io.ebeaninternal.server.type;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.text.json.EJson;
import io.ebeanservice.docstore.api.mapping.DocPropertyType;

import javax.persistence.PersistenceException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Array;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Type mapped for DB ARRAY type (Postgres only effectively).
 */
public class ScalarTypeArraySet extends ScalarTypeJsonCollection<Set> implements ScalarTypeArray {

  private static ScalarTypeArraySet UUID = new ScalarTypeArraySet("uuid", DocPropertyType.UUID, ArrayElementConverter.UUID);
  private static ScalarTypeArraySet LONG = new ScalarTypeArraySet("bigint", DocPropertyType.LONG, ArrayElementConverter.LONG);
  private static ScalarTypeArraySet INTEGER = new ScalarTypeArraySet("integer", DocPropertyType.INTEGER, ArrayElementConverter.INTEGER);
  private static ScalarTypeArraySet DOUBLE = new ScalarTypeArraySet("float", DocPropertyType.DOUBLE, ArrayElementConverter.DOUBLE);
  private static ScalarTypeArraySet STRING = new ScalarTypeArraySet("varchar", DocPropertyType.TEXT, ArrayElementConverter.STRING);

  static PlatformArrayTypeFactory factory() {
    return new Factory();
  }

  static class Factory implements PlatformArrayTypeFactory {

    /**
     * Return the ScalarType to use based on the List's generic parameter type.
     */
    @Override
    public ScalarTypeArraySet typeFor(Type valueType) {
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
      throw new IllegalArgumentException("Type [" + valueType + "] not supported for @DbArray mapping on set");
    }
  }

  private final String arrayType;

  private final ArrayElementConverter converter;

  public ScalarTypeArraySet(String arrayType, DocPropertyType docPropertyType, ArrayElementConverter converter) {
    super(Set.class, Types.ARRAY, docPropertyType);
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
  public String getDbColumnDefn() {
    return arrayType + "[]";
  }

  @SuppressWarnings("unchecked")
  private Set fromArray(Object[] array1) {
    Set set = new LinkedHashSet();
    for (Object element : array1) {
      set.add(converter.toElement(element));
    }
    return new ModifyAwareSet(set);
  }

  protected Object[] toArray(Set value) {
    return value.toArray();
  }

  @Override
  public Set read(DataReader reader) throws SQLException {
    Array array = reader.getArray();
    if (array == null) {
      return null;
    } else {
      return fromArray((Object[]) array.getArray());
    }
  }

  @Override
  public void bind(DataBind bind, Set value) throws SQLException {
    if (value == null) {
      bind.setNull(Types.ARRAY);
    } else {
      bind.setArray(arrayType, toArray(value));
    }
  }

  @Override
  public String formatValue(Set value) {
    try {
      return EJson.write(value);
    } catch (IOException e) {
      throw new PersistenceException("Failed to format List into JSON content", e);
    }
  }

  @Override
  public Set parse(String value) {
    try {
      return EJson.parseSet(value, false);
    } catch (IOException e) {
      throw new PersistenceException("Failed to parse JSON content as List: [" + value + "]", e);
    }
  }

  @Override
  public Set jsonRead(JsonParser parser) throws IOException {
    return EJson.parseSet(parser, parser.getCurrentToken());
  }

  @Override
  public void jsonWrite(JsonGenerator writer, Set value) throws IOException {
    EJson.write(value, writer);
  }

}
