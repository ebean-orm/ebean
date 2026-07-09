package io.ebeaninternal.server.type;

import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.ebean.annotation.MutationDetection;
import io.ebean.core.type.ScalarType;
import io.ebean.text.TextException;
import io.ebean.text.json.EJson;
import io.ebeaninternal.json.ModifyAwareMap;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Type which maps {@code Map<Enum,Object>} to JSON.
 * <p>
 * The enum keys are (de)serialized via their {@link ScalarType} (honouring custom
 * {@code @DbEnumValue} mappings), then delegated to {@link ScalarTypeJsonMap} for all
 * storage, platform and value handling.
 */
@SuppressWarnings("rawtypes")
final class ScalarTypeJsonMapEnum<T extends Enum<T>> extends ScalarTypeJsonMap {

  private final ScalarType<T> enumType;

  static ScalarType<?> typeFor(boolean postgres, int dbType, ScalarType<? extends Enum<?>> enumType, MutationDetection mutationDetection) {
    return new ScalarTypeJsonMapEnum<>(storageFor(postgres, dbType), enumType, mutationDetection);
  }

  @SuppressWarnings("unchecked")
  private ScalarTypeJsonMapEnum(JsonStorage storage, ScalarType<? extends Enum> enumType, MutationDetection mutationDetection) {
    super(storage, mutationDetection);
    this.enumType = (ScalarType<T>) enumType;
  }

  @Override
  Map readJson(String rawJson) {
    return parse(rawJson);
  }

  @Override
  public Map parse(String value) {
    try {
      return toEnumKeys(EJson.parseObject(value, true));
    } catch (IOException e) {
      throw new TextException("Failed to parse JSON [{}] as Map with enum keys", value, e);
    }
  }

  @Override
  public String formatValue(Map value) {
    try {
      return EJson.write(toStringKeys(value));
    } catch (IOException e) {
      throw new TextException(e);
    }
  }

  @Override
  public Map jsonRead(JsonReader parser) throws IOException {
    return toEnumKeys(EJson.parseObject(parser, parser.currentToken()));
  }

  @Override
  public void jsonWrite(JsonWriter writer, Map value) throws IOException {
    EJson.write(toStringKeys(value), writer);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> toStringKeys(Map value) {
    Map<String, Object> stringKeyMap = new LinkedHashMap<>();
    for (Object o : value.entrySet()) {
      Map.Entry e = (Map.Entry) o;
      stringKeyMap.put(enumType.formatValue((T) e.getKey()), e.getValue());
    }
    return stringKeyMap;
  }

  @SuppressWarnings("unchecked")
  private Map toEnumKeys(Map<String, Object> stringKeyMap) {
    if (stringKeyMap == null) {
      return null;
    }
    Map enumKeyMap = new LinkedHashMap();
    for (Map.Entry<String, Object> e : stringKeyMap.entrySet()) {
      enumKeyMap.put(enumType.parse(e.getKey()), e.getValue());
    }
    return stringKeyMap instanceof ModifyAwareMap ? new ModifyAwareMap(enumKeyMap) : enumKeyMap;
  }
}
