package io.ebeaninternal.server.querydefn;

import io.ebeaninternal.server.util.DSelectColumnsParser;

import java.util.Set;

/**
 * Parses the path properties string.
 */
class OrmQueryPropertiesParser {

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
  public static Response parse(String rawProperties) {
    return new OrmQueryPropertiesParser(rawProperties).parse();
  }

  private final String inputProperties;

  private OrmQueryPropertiesParser(String inputProperties) {
    this.inputProperties = inputProperties;
  }

  /**
   * Parse the raw string properties input.
   */
  private Response parse() {
    if (inputProperties == null || inputProperties.isEmpty()) {
      return EMPTY;
    }
    if (inputProperties.equals("*")) {
      return ALL;
    }
    return new Response(false, splitRawSelect(inputProperties));
  }

  /**
   * Split allowing 'dynamic function based properties'.
   */
  private Set<String> splitRawSelect(String inputProperties) {
    return DSelectColumnsParser.parse(inputProperties);
  }

}
