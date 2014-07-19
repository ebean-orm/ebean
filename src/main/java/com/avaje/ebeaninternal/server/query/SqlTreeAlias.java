package com.avaje.ebeaninternal.server.query;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.el.ElPropertyDeploy;

/**
 * Special Map of the logical property joins to table alias.
 * 
 * @author rbygrave
 */
public class SqlTreeAlias {

  private int counter;

  private int manyWhereCounter;

  private TreeSet<String> joinProps = new TreeSet<String>();

  private HashSet<String> embeddedPropertyJoins;

  private TreeSet<String> manyWhereJoinProps = new TreeSet<String>();

  private HashMap<String, String> aliasMap = new HashMap<String, String>();

  private HashMap<String, String> manyWhereAliasMap = new HashMap<String, String>();

  private final String rootTableAlias;

  public SqlTreeAlias(String rootTableAlias) {
    this.rootTableAlias = rootTableAlias;
  }

  /**
   * Add joins to support where predicates
   */
  public void addManyWhereJoins(Set<String> manyWhereJoins) {
    if (manyWhereJoins != null) {
      for (String include : manyWhereJoins) {
        addPropertyJoin(include, manyWhereJoinProps);
      }
    }
  }

  private void addEmbeddedPropertyJoin(String embProp) {
    if (embeddedPropertyJoins == null) {
      embeddedPropertyJoins = new HashSet<String>();
    }
    embeddedPropertyJoins.add(embProp);
  }

  /**
   * Add joins.
   */
  public void addJoin(Set<String> propJoins, BeanDescriptor<?> desc) {
    if (propJoins != null) {
      for (String propJoin : propJoins) {
        ElPropertyDeploy elProp = desc.getElPropertyDeploy(propJoin);
        if (elProp != null && elProp.getBeanProperty().isEmbedded()) {
          addEmbeddedPropertyJoin(propJoin);

        } else {
          addPropertyJoin(propJoin, joinProps);
        }
      }
    }
  }

  private void addPropertyJoin(String include, TreeSet<String> set) {
    if (set.add(include)) {
      String[] split = SplitName.split(include);
      if (split[0] != null) {
        addPropertyJoin(split[0], set);
      }
    }
  }

  /**
   * Build a set of table alias for the given bean and fetch joined properties.
   */
  public void buildAlias() {

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
        String alias = getTableAlias(split[0]);
        aliasMap.put(propJoin, alias);
      }
    }
  }

  private String calcAlias(String prefix) {

    String alias = nextTableAlias();
    aliasMap.put(prefix, alias);
    return alias;
  }

  private String calcAliasManyWhere(String prefix) {

    String alias = nextManyWhereTableAlias();
    manyWhereAliasMap.put(prefix, alias);
    return alias;
  }

  /**
   * Return the table alias for a given property name.
   */
  public String getTableAlias(String prefix) {
    if (prefix == null) {
      return rootTableAlias;
    } else {
      String s = aliasMap.get(prefix);
      if (s == null) {
        return calcAlias(prefix);
      }
      return s;
    }
  }

  /**
   * Return an alias using "Many where joins".
   */
  public String getTableAliasManyWhere(String prefix) {
    if (prefix == null) {
      return rootTableAlias;
    }
    String s = manyWhereAliasMap.get(prefix);
    if (s == null) {
      s = aliasMap.get(prefix);
    }
    if (s == null) {
      String msg = "Could not determine table alias for [" + prefix + "] manyMap[" + manyWhereAliasMap + "] aliasMap["+ aliasMap + "]";
      throw new RuntimeException(msg);
    }
    return s;
  }

  /**
   * Parse for where clauses that uses "Many where joins"
   */
  public String parseWhere(String clause) {
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
  private String parseRootAlias(String clause) {

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
}
