package io.ebeaninternal.server.type;

import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.DocPropertyType;
import io.ebean.core.type.ScalarTypeBase;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Base for the built-in JSON value types (Map, List, Set).
 * <p>
 * Holds the common read / bind / L2 cache plumbing and delegates:
 * <ul>
 *   <li>the storage concern (VARCHAR / CLOB / BLOB / Postgres) to a {@link JsonStorage}</li>
 *   <li>the value marshalling concern to the subclass (which uses the avaje JsonMapper
 *       backed {@code EJson} facade)</li>
 * </ul>
 * This removes the previous explosion of per-storage and per-platform subclasses.
 */
abstract class ScalarTypeJsonValue<T> extends ScalarTypeBase<T> {

  protected final JsonStorage storage;
  protected final boolean keepSource;
  private final boolean nullable;
  private final String emptyJson;
  private final DocPropertyType docType;

  /**
   * @param emptyJson JSON bound when the value is null and the property is not nullable
   *                  (e.g. {@code "[]"} for collections), or null to always bind SQL null.
   */
  ScalarTypeJsonValue(Class<T> type, int jdbcType, JsonStorage storage, boolean keepSource,
                      boolean nullable, String emptyJson, DocPropertyType docType) {
    super(type, false, jdbcType);
    this.storage = storage;
    this.keepSource = keepSource;
    this.nullable = nullable;
    this.emptyJson = emptyJson;
    this.docType = docType;
  }

  /**
   * Marshal the raw JSON read from the DB into the bean value (the load path - typically
   * returns a modify-aware collection).
   */
  abstract T readJson(String rawJson);

  @Override
  public final boolean mutable() {
    return true;
  }

  @Override
  public final boolean isDirty(Object value) {
    return TypeJsonManager.checkIsDirty(value);
  }

  @Override
  public final boolean jsonMapper() {
    return keepSource;
  }

  @Override
  public final T read(DataReader reader) throws SQLException {
    String rawJson = storage.read(reader);
    if (keepSource) {
      reader.pushJson(rawJson);
    }
    if (rawJson == null) {
      return null;
    }
    return readJson(rawJson);
  }

  @Override
  public final void bind(DataBinder binder, T value) throws SQLException {
    String rawJson = keepSource ? binder.popJson() : null;
    if (rawJson == null && value != null) {
      rawJson = formatValue(value);
    }
    if (value == null) {
      if (nullable || emptyJson == null) {
        storage.bindNull(binder);
      } else {
        storage.bind(binder, emptyJson);
      }
    } else {
      storage.bind(binder, rawJson);
    }
  }

  @Override
  public final Object toJdbcType(Object value) {
    return value;
  }

  @Override
  @SuppressWarnings("unchecked")
  public final T toBeanType(Object value) {
    return (T) value;
  }

  @Override
  public final DocPropertyType docType() {
    return docType;
  }

  @Override
  public final T readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    }
    return parse(dataInput.readUTF());
  }

  @Override
  public final void writeData(DataOutput dataOutput, T value) throws IOException {
    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      ScalarHelp.writeUTF(dataOutput, formatValue(value));
    }
  }
}
