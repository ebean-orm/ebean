package io.ebeaninternal.server.query;

import io.ebean.util.SplitName;
import io.ebeaninternal.api.SpiQuery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Special Map of the logical property joins to table alias.
 */
final class SqlTreeAlias {

  private final SpiQuery.TemporalMode temporalMode;
  private final TreeSet<String> joinProps = new TreeSet<>();
  private HashSet<String> embeddedPropertyJoins;
  private final TreeSet<String> manyWhereJoinProps = new TreeSet<>();
  private final HashMap<String, String> aliasMap = new HashMap<>();
  private final HashMap<String, String> manyWhereAliasMap = new HashMap<>();
  private final String rootTableAlias;
  private int counter;
  private int manyWhereCounter;

  SqlTreeAlias(String rootTableAlias, SpiQuery.TemporalMode temporalMode) {
    this.rootTableAlias = rootTableAlias;
    this.temporalMode = temporalMode;
  }

  /**
   * Add joins to support where predicates
   */
  void addManyWhereJoins(Set<String> manyWhereJoins, STreeType desc) {
    if (manyWhereJoins != null) {
      for (String include : manyWhereJoins) {
        addPropertyJoin(include, manyWhereJoinProps, desc);
      }
    }
  }

  private boolean addEmbeddedPropertyJoin(String embProp) {
    if (embeddedPropertyJoins == null) {
      embeddedPropertyJoins = new HashSet<>();
    }
    return embeddedPropertyJoins.add(embProp);
  }

  /**
   * Add joins.
   */
  public void addJoin(Set<String> propJoins, STreeType desc) {
    if (propJoins == null) {
        return;
    }
    for (String propJoin : propJoins) {
      addPropertyJoin(propJoin, joinProps, desc);
    }
  }

  private void addPropertyJoin(String include, TreeSet<String> set, STreeType desc) {
    boolean added = false;
    if (desc.isEmbeddedPath(include)) {
      added = addEmbeddedPropertyJoin(include);
    } else {
      added = set.add(include);
    }
    if (added) {
      String[] split = SplitName.split(include);
      if (split[0] != null) {
        addPropertyJoin(split[0], set, desc);
      }
    }
  }

  /**
   * Build a set of table alias for the given bean and fetch joined properties.
   */
  void buildAlias() {
    for (String joinProp : joinProps) {
      calcAlias(joinProp);
    }
    for (String joinProp : manyWhereJoinProps) {
      calcAliasManyWhere(joinProp);
    }
    mapEmbeddedPropertyAlias();
  }

  private void mapEmbeddedPropertyAlias() {
    if (embeddedPropertyJoins != null) {
      for (String propJoin : embeddedPropertyJoins) {
        String[] split = SplitName.split(propJoin);
        // the table alias of the parent path
        String alias = tableAliasManyWhere(split[0]);
        aliasMap.put(propJoin, alias);
      }
    }
  }

  private String calcAlias(String prefix) {
    String alias = nextTableAlias();
    aliasMap.put(prefix, alias);
    return alias;
  }

  private void calcAliasManyWhere(String prefix) {
    String alias = nextManyWhereTableAlias();
    manyWhereAliasMap.put(prefix, alias);
  }

  /**
   * Return the table alias for a given property name.
   */
  String tableAlias(String prefix) {
    if (prefix == null) {
      return rootTableAlias;
    } else {
      String alias = aliasMap.get(prefix);
      return alias != null ? alias : calcAlias(prefix);
    }
  }

  /**
   * Return an alias using "Many where joins".
   */
  String tableAliasManyWhere(String prefix) {
    if (prefix == null) {
      return rootTableAlias;
    }
    String alias = manyWhereAliasMap.get(prefix);
    if (alias == null) {
      alias = aliasMap.get(prefix);
    }
    if (alias == null) {
      throw new RuntimeException("Could not determine table alias for " + prefix);
    }
    return alias;
  }

  /**
   * Parse for where clauses that uses "Many where joins"
   */
  String parseWhere(String clause) {
    clause = parseRootAlias(clause);
    clause = parseAliasMap(clause, manyWhereAliasMap);
    return parseAliasMap(clause, aliasMap);
  }

  /**
   * Parse without using any extra "Many where joins".
   */
  public String parse(String clause) {
    clause = parseRootAlias(clause);
    return parseAliasMap(clause, aliasMap);
  }

  /**
   * Parse the clause replacing the table alias place holders.
   */
  String parseRootAlias(String clause) {
    if (rootTableAlias == null) {
      return clause.replace("${}", "");
    } else {
      return clause.replace("${}", rootTableAlias + ".");
    }
  }

  /**
   * Parse the clause replacing the table alias place holders.
   */
  private String parseAliasMap(String clause, HashMap<String, String> parseAliasMap) {
    for (Map.Entry<String, String> e : parseAliasMap.entrySet()) {
      String k = "${" + e.getKey() + "}";
      clause = clause.replace(k, e.getValue() + ".");
    }
    return clause;
  }

  /**
   * Return the next valid table alias given the preferred table alias.
   */
  private String nextTableAlias() {
    return "t" + (++counter);
  }

  private String nextManyWhereTableAlias() {
    return "u" + (++manyWhereCounter);
  }

  /**
   * Return true if there are joins included in the query.
   */
  boolean isIncludeJoins() {
    return !aliasMap.isEmpty() || !manyWhereAliasMap.isEmpty();
  }

  boolean isIncludeSoftDelete() {
    return temporalMode == SpiQuery.TemporalMode.SOFT_DELETED;
  }
}
