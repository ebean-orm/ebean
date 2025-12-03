package io.ebeaninternal.server.query;

import io.ebean.util.SplitName;
import io.ebeaninternal.api.SpiQuery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Special Map of the logical property joins to table alias.
 */
final class SqlTreeAlias {

  private final SpiQuery.TemporalMode temporalMode;
  private final TreeSet<String> joinProps = new TreeSet<>();
  // embedded property as key, and true if many-where-property
  private HashMap<String, Boolean> embeddedPropertyJoins;
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
        addPropertyJoin(include, manyWhereJoinProps, desc, true);
      }
    }
  }

  private void addEmbeddedPropertyJoin(String embProp, Boolean isManyWhere) {
    if (embeddedPropertyJoins == null) {
      embeddedPropertyJoins = new HashMap<>();
    }
    embeddedPropertyJoins.put(embProp, isManyWhere);
  }

  /**
   * Add joins.
   */
  public void addJoin(Set<String> propJoins, STreeType desc) {
    if (propJoins != null) {
      for (String propJoin : propJoins) {
        addPropertyJoin(propJoin, joinProps, desc, false);
      }
    }
  }

  private void addPropertyJoin(String include, TreeSet<String> set, STreeType desc, Boolean isManyWhere) {
    if (include == null) {
      return;
    }
    String[] split = SplitName.split(include);
    if (desc.isEmbeddedPath(include)) {
      addEmbeddedPropertyJoin(include, isManyWhere);
      addPropertyJoin(split[0], set, desc, isManyWhere);
    } else if (set.add(include)) {
      addPropertyJoin(split[0], set, desc, isManyWhere);
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
      for (Map.Entry<String,Boolean> propJoin : embeddedPropertyJoins.entrySet()) {
        String[] split = SplitName.split(propJoin.getKey());
        // the table alias of the parent path
        if (Boolean.TRUE.equals(propJoin.getValue())) {
          manyWhereAliasMap.put(propJoin.getKey(), tableAliasManyWhere(split[0]));
        } else {
          aliasMap.put(propJoin.getKey(), tableAlias(split[0]));
        }
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
