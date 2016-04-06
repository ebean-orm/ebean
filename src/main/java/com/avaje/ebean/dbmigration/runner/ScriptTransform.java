package com.avaje.ebean.dbmigration.runner;

import java.util.HashMap;
import java.util.Map;

/**
 * Transforms a SQL script given a map of key/value substitutions.
 */
class ScriptTransform {

  /**
   * Transform just ${table} with the table name.
   */
  public static String table(String tableName, String script) {
    return script.replace("${table}", tableName);
  }

  private final Map<String,String> placeholders = new HashMap<String, String>();

  ScriptTransform(Map<String,String> map) {
    for (Map.Entry<String, String> entry : map.entrySet()) {
      placeholders.put(wrapKey(entry.getKey()), entry.getValue());
    }
  }

  private String wrapKey(String key) {
    return "${"+key+"}";
  }

  /**
   * Return true if this contains no placeholders.
   */
  boolean isEmpty() {
    return placeholders.isEmpty();
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
