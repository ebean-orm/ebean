package io.ebeaninternal.server.querydefn;

import io.ebean.FetchConfig;
import io.ebean.util.StringHelper;
import io.ebeaninternal.server.util.DSelectColumnsParser;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * Parses the path properties string.
 */
class OrmQueryPropertiesParser {

  private static Response EMPTY = new Response();

  /**
   * Immutable response of the parsed properties and options.
   */
  static class Response {

    final boolean readOnly;
    final boolean cache;
    final FetchConfig fetchConfig;
    final String properties;
    final LinkedHashSet<String> included;

    Response(boolean readOnly, boolean cache, int queryFetchBatch, int lazyFetchBatch, String properties, LinkedHashSet<String> included) {
      this.readOnly = readOnly;
      this.cache = cache;
      this.properties = properties;
      this.included = included;
      if (lazyFetchBatch > -1 || queryFetchBatch > -1) {
        this.fetchConfig = new FetchConfig().lazy(lazyFetchBatch).query(queryFetchBatch);
      } else {
        this.fetchConfig = OrmQueryProperties.DEFAULT_FETCH;
      }
    }

    Response() {
      this.readOnly = false;
      this.cache = false;
      this.fetchConfig = OrmQueryProperties.DEFAULT_FETCH;
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

  private String inputProperties;

  private String outputProperties = "";
  private boolean allProperties;
  private boolean readOnly;
  private boolean cache;
  private int queryFetchBatch = -1;
  private int lazyFetchBatch = -1;

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
    int pos = inputProperties.indexOf("+readonly");
    if (pos > -1) {
      inputProperties = StringHelper.replaceString(inputProperties, "+readonly", "");
      readOnly = true;
    }
    pos = inputProperties.indexOf("+cache");
    if (pos > -1) {
      inputProperties = StringHelper.replaceString(inputProperties, "+cache", "");
      cache = true;
    }
    pos = inputProperties.indexOf("+query");
    if (pos > -1) {
      queryFetchBatch = parseBatchHint(pos, "+query");
    }
    pos = inputProperties.indexOf("+lazy");
    if (pos > -1) {
      lazyFetchBatch = parseBatchHint(pos, "+lazy");
    }

    LinkedHashSet<String> included = parseIncluded();
    String properties = (allProperties) ? "*" : outputProperties;
    return new Response(readOnly, cache, queryFetchBatch, lazyFetchBatch, properties, included);
  }

  /**
   * Parse the include separating by comma or semicolon.
   */
  private LinkedHashSet<String> parseIncluded() {

    inputProperties = inputProperties.trim();
    if (inputProperties.isEmpty()) {
      // default properties
      return null;
    }
    if (inputProperties.equals("*")) {
      // explicit all properties
      allProperties = true;
      return null;
    }

    List<String> res = splitRawSelect(inputProperties);

    StringBuilder sb = new StringBuilder(70);
    LinkedHashSet<String> propertySet = new LinkedHashSet<>(res.size() * 2);

    int count = 0;
    String temp;
    for (String re : res) {
      temp = re.trim();
      if (!temp.isEmpty()) {
        if (count > 0) {
          sb.append(",");
        }
        sb.append(temp);
        propertySet.add(temp);
        count++;
      }
    }

    if (propertySet.isEmpty()) {
      // default properties
      return null;
    }

    if (propertySet.contains("*")) {
      // explicit all properties
      allProperties = true;
      return null;
    }

    // partial properties
    outputProperties = sb.toString();
    return propertySet;
  }

  /**
   * Split allowing 'dynamic function based properties'.
   */
  private List<String> splitRawSelect(String inputProperties) {
    return DSelectColumnsParser.parse(inputProperties);
  }

  private int parseBatchHint(int pos, String option) {

    int startPos = pos + option.length();
    int endPos = findEndPos(startPos, inputProperties);
    if (endPos == -1) {
      inputProperties = StringHelper.replaceString(inputProperties, option, "");
      return 0;

    } else {

      String batchParam = inputProperties.substring(startPos + 1, endPos);

      if (endPos + 1 >= inputProperties.length()) {
        inputProperties = inputProperties.substring(0, pos);
      } else {
        inputProperties = inputProperties.substring(0, pos) + inputProperties.substring(endPos + 1);
      }
      return Integer.parseInt(batchParam);
    }
  }

  private int findEndPos(int pos, String props) {

    if (pos < props.length()) {
      if (props.charAt(pos) == '(') {
        int endPara = props.indexOf(')', pos + 1);
        if (endPara == -1) {
          throw new RuntimeException("Error could not find ')' in " + props + " after position " + pos);
        }
        return endPara;
      }
    }
    return -1;
  }
}
