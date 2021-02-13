package io.ebeaninternal.server.querydefn;

import io.ebeaninternal.server.util.DSelectColumnsParser;

import java.util.Set;

/**
 * Parses the path properties string.
 */
class OrmQueryPropertiesParser {

  private static final Response EMPTY = new Response();

  /**
   * Immutable response of the parsed properties and options.
   */
  static class Response {

    final String properties;
    final Set<String> included;

    private Response(String properties, Set<String> included) {
      this.properties = properties;
      this.included = included;
    }

    private Response() {
      this.properties = "";
      this.included = null;
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
  private boolean allProperties;

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
      return new Response("*", null);
    }
    Set<String> fields = splitRawSelect(inputProperties);
    for (String val : fields) {
      if (val.equals("*")) {
        allProperties = true;
        break;
      }
    }
    String properties = allProperties ? "*" : inputProperties;
    if (fields.isEmpty()) {
      fields = null;
    }
    return new Response(properties, fields);
  }

  /**
   * Split allowing 'dynamic function based properties'.
   */
  private Set<String> splitRawSelect(String inputProperties) {
    return DSelectColumnsParser.parse(inputProperties);
  }

}
