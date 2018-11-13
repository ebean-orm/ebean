package io.ebeaninternal.dbmigration;

import java.util.HashMap;
import java.util.Map;

/**
 * Transforms a SQL script given a map of key/value substitutions.
 */
class ScriptTransform {

  /**
   * Transform just ${table} with the table name.
   */
  static String replace(String key, String value, String script) {
    return script.replace(key, value);
  }

  private final Map<String,String> placeholders = new HashMap<>();

  ScriptTransform(Map<String,String> map) {
    for (Map.Entry<String, String> entry : map.entrySet()) {
      placeholders.put(wrapKey(entry.getKey()), entry.getValue());
    }
  }

  private String wrapKey(String key) {
    return "${"+key+"}";
  }

  /**
   * Transform the script replacing placeholders in the form <code>${key}</code> with <code>value</code>.
   */
  String transform(String source) {

    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
      source = source.replace(entry.getKey(), entry.getValue());
    }
    return source;
  }
}
