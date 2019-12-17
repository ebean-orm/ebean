package io.ebeaninternal.server.type;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.text.TextException;
import io.ebean.text.json.EJson;
import io.ebeaninternal.json.ModifyAwareList;
import io.ebeanservice.docstore.api.mapping.DocPropertyType;

import javax.persistence.PersistenceException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.EMPTY_LIST;

/**
 * Type mapped for DB ARRAY type (Postgres only effectively).
 */
@SuppressWarnings("rawtypes")
public class ScalarTypeArrayList extends ScalarTypeArrayBase<List> implements ScalarTypeArray {

  static PlatformArrayTypeFactory factory() {
    return new Factory();
  }

  static class Factory implements PlatformArrayTypeFactory {

    private final Map<String, ScalarTypeArrayList> cache = new HashMap<>();

    /**
     * Return the ScalarType to use based on the List's generic parameter type.
     */
    @Override
    public ScalarTypeArrayList typeFor(Type valueType, boolean nullable) {
      synchronized (this) {
        String key = valueType + ":" + nullable;
        if (valueType.equals(UUID.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArrayList(nullable, "uuid", DocPropertyType.UUID, ArrayElementConverter.UUID));
        }
        if (valueType.equals(Long.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArrayList(nullable, "bigint", DocPropertyType.LONG, ArrayElementConverter.LONG));
        }
        if (valueType.equals(Integer.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArrayList(nullable, "integer", DocPropertyType.INTEGER, ArrayElementConverter.INTEGER));
        }
        if (valueType.equals(Double.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArrayList(nullable, "float", DocPropertyType.DOUBLE, ArrayElementConverter.DOUBLE));
        }
        if (valueType.equals(String.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArrayList(nullable, "varchar", DocPropertyType.TEXT, ArrayElementConverter.STRING));
        }
        throw new IllegalArgumentException("Type [" + valueType + "] not supported for @DbArray mapping");
      }
    }

    @Override
    public ScalarTypeArrayList typeForEnum(ScalarType<?> scalarType, boolean nullable) {
      return new ScalarTypeArrayList(nullable, arrayTypeFor(scalarType), scalarType.getDocType(), new ArrayElementConverter.EnumConverter(scalarType));
    }
  }

  private final String arrayType;

  private final ArrayElementConverter converter;

  public ScalarTypeArrayList(boolean nullable, String arrayType, DocPropertyType docPropertyType, ArrayElementConverter converter) {
    super(List.class, Types.ARRAY, docPropertyType, nullable);
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
  @Override
  protected List fromArray(Object[] array1) {
    List list = new ArrayList(array1.length);
    for (Object element : array1) {
      if (element == null) {
        list.add(null);
      } else {
        list.add(converter.toElement(element));
      }
    }
    return new ModifyAwareList(list);
  }

  protected Object[] toArray(List value) {
    return converter.toDbArray(value.toArray());
  }

  @Override
  public void bind(DataBind bind, List value) throws SQLException {
    if (value == null) {
      bindNull(bind);
    } else {
      bind.setArray(arrayType, toArray(value));
    }
  }

  @Override
  protected void bindNull(DataBind bind) throws SQLException {
    if (nullable) {
      bind.setNull(Types.ARRAY);
    } else {
      bind.setArray(arrayType, toArray(EMPTY_LIST));
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
      throw new TextException("Failed to parse JSON [{}] as List", value, e);
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
