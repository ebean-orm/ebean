package io.ebeaninternal.server.querydefn;

import io.ebeaninternal.server.util.DSelectColumnsParser;

import java.util.Set;

/**
 * Parses the path properties string.
 */
final class OrmQueryPropertiesParser {

  private static final Response EMPTY = new Response(false, null);
  private static final Response ALL = new Response(true, null);

  /**
   * Immutable response of the parsed properties and options.
   */
  static class Response {

    final boolean allProperties;
    final Set<String> included;

    private Response(boolean allProperties, Set<String> included) {
      this.allProperties = allProperties;
      this.included = included;
    }
  }

  /**
   * Parses the path properties string returning the parsed properties and options.
   * In general it is comma delimited with some special strings like +lazy(20).
   */
  static Response parse(String rawProperties) {
    if (rawProperties == null || rawProperties.isEmpty()) {
      return EMPTY;
    }
    if (rawProperties.equals("*")) {
      return ALL;
    }
    final Set<String> included = splitRawSelect(rawProperties);
    if (included.contains("*")) {
      return ALL;
    }
    return new Response(false, included);
  }

  /**
   * Split allowing 'dynamic function based properties'.
   */
  private static Set<String> splitRawSelect(String inputProperties) {
    return DSelectColumnsParser.parse(inputProperties);
  }

}
