package io.ebeaninternal.server.type;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.text.json.EJson;
import io.ebeaninternal.json.ModifyAwareSet;
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
public class ScalarTypeArraySet<T> extends ScalarTypeJsonCollection<Set<T>> implements ScalarTypeArray {

  private static final ScalarTypeArraySet<UUID> UUID = new ScalarTypeArraySet<>("uuid", DocPropertyType.UUID, ArrayElementConverter.UUID);
  private static final ScalarTypeArraySet<Long> LONG = new ScalarTypeArraySet<>("bigint", DocPropertyType.LONG, ArrayElementConverter.LONG);
  private static final ScalarTypeArraySet<Integer> INTEGER = new ScalarTypeArraySet<>("integer", DocPropertyType.INTEGER, ArrayElementConverter.INTEGER);
  private static final ScalarTypeArraySet<Double> DOUBLE = new ScalarTypeArraySet<>("float", DocPropertyType.DOUBLE, ArrayElementConverter.DOUBLE);
  private static final ScalarTypeArraySet<String> STRING = new ScalarTypeArraySet<>("varchar", DocPropertyType.TEXT, ArrayElementConverter.STRING);

  static PlatformArrayTypeFactory factory() {
    return new Factory();
  }

  static class Factory implements PlatformArrayTypeFactory {

    /**
     * Return the ScalarType to use based on the List's generic parameter type.
     */
    @Override
    public ScalarTypeArraySet<?> typeFor(Type valueType) {
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
      throw new IllegalArgumentException("Type [" + valueType + "] not supported for @DbArray mapping on list");
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ScalarTypeArraySet typeForEnum(ScalarType<?> scalarType) {
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
      return new ScalarTypeArraySet(arrayType, scalarType.getDocType(), new ArrayElementConverter.EnumConverter(scalarType));
    }
  }

  private final String arrayType;

  private final ArrayElementConverter<T> converter;

  @SuppressWarnings({"unchecked", "rawtypes"})
  public ScalarTypeArraySet(String arrayType, DocPropertyType docPropertyType, ArrayElementConverter<T> converter) {
    super((Class) Set.class, Types.ARRAY, docPropertyType);
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

  private Set<T> fromArray(Object[] array1) {
    Set<T> set = new LinkedHashSet<>();
    for (Object element : array1) {
      set.add(converter.toElement(element));
    }
    return new ModifyAwareSet<>(set);
  }

  protected Object[] toArray(Set<T> value) {
    return converter.toDbArray(value.toArray());
  }

  @Override
  public Set<T> read(DataReader reader) throws SQLException {
    Array array = reader.getArray();
    if (array == null) {
      return null;
    } else {
      return fromArray((Object[]) array.getArray());
    }
  }

  @Override
  public void bind(DataBind bind, Set<T> value) throws SQLException {
    if (value == null) {
      bind.setNull(Types.ARRAY);
    } else {
      bind.setArray(arrayType, toArray(value));
    }
  }

  @Override
  public String formatValue(Set<T> value) {
    try {
      return EJson.write(value);
    } catch (IOException e) {
      throw new PersistenceException("Failed to format List into JSON content", e);
    }
  }

  @Override
  public Set<T> parse(String value) {
    try {
      return EJson.parseSet(value, false);
    } catch (IOException e) {
      throw new PersistenceException("Failed to parse JSON content as List: [" + value + "]", e);
    }
  }

  @Override
  public Set<T> jsonRead(JsonParser parser) throws IOException {
    return EJson.parseSet(parser, parser.getCurrentToken());
  }

  @Override
  public void jsonWrite(JsonGenerator writer, Set<T> value) throws IOException {
    EJson.write(value, writer);
  }

}
