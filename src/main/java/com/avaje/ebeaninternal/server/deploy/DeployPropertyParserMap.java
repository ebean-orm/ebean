package com.avaje.ebeaninternal.server.deploy;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Converts logical property names to database columns using a Map.
 */
public final class DeployPropertyParserMap extends DeployParser {

  private final Map<String, String> map;

  public DeployPropertyParserMap(Map<String, String> map) {
    this.map = map;
  }

  /**
   * Returns null for raw sql queries.
   */
  public Set<String> getIncludes() {
    return Collections.emptySet();
  }

  public String convertWord() {
    String r = getDeployWord(word);
    return r == null ? word : r;
  }

  @Override
  public String getDeployWord(String expression) {

    String deployExpr = map.get(expression);
    if (deployExpr == null) {
      return null;
    } else {
      return deployExpr;
    }
  }

}
