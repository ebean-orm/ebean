package io.ebeaninternal.dbmigration;

import java.util.HashMap;
import java.util.Map;

/**
 * Joins placeholder map and comma/equals delimited string.
 */
class PlaceholderBuilder {

  private final Map<String,String> map = new HashMap<>();

  /**
   * Create with raw comma and equals delimited pairs plus map of key value pairs.
   */
  static Map<String,String> build(String commaDelimited, Map<String,String> placeholders) {

    PlaceholderBuilder builder = new PlaceholderBuilder();
    builder.add(commaDelimited);
    builder.add(placeholders);

    return builder.map;
  }

  private PlaceholderBuilder() {

  }

  /**
   * Add a comma and equals delimited string to parse for key value pairs.
   */
  private void add(String commaDelimited) {

    if (commaDelimited != null) {
      String[] split = commaDelimited.split("[,;]");
      for (String keyValue : split) {
        String[] pair = keyValue.split("=");
        if (pair.length == 2) {
          map.put(pair[0].trim(), pair[1].trim());
        }
      }
    }
  }

  /**
   * Add a map of key value placeholder pairs.
   */
  private void add(Map<String,String> placeholders) {
    if (placeholders != null) {
      map.putAll(placeholders);
    }
  }
}
