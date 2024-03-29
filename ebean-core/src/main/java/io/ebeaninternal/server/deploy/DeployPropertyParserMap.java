package io.ebeaninternal.server.deploy;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Converts logical property names to database columns using a Map.
 */
public class DeployPropertyParserMap extends DeployParser {

  private final Map<String, String> map;

  public DeployPropertyParserMap(Map<String, String> map) {
    this.map = map;
  }

  /**
   * Returns null for raw sql queries.
   */
  @Override
  public Set<String> includes() {
    return Collections.emptySet();
  }

  @Override
  public String convertWord() {
    String r = deployWord(word);
    return r == null ? word : r;
  }

  @Override
  public String deployWord(String expression) {
    return map.get(expression);
  }

}
