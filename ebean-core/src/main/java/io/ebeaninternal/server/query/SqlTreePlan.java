package io.ebeaninternal.server.query;

import java.util.Set;

final class SqlTreePlan {

  private final SqlTreeLoad rootNode;
  private final STreePropertyAssocMany manyProperty;
  private final STreeProperty[] encryptedProps;
  private final Set<String> dependentTables;

  SqlTreePlan(SqlTreeLoad rootNode, STreePropertyAssocMany manyProperty, STreeProperty[] encryptedProps, Set<String> dependentTables) {
    this.rootNode = rootNode;
    this.manyProperty = manyProperty;
    this.encryptedProps = encryptedProps;
    this.dependentTables = dependentTables;
  }

  SqlTreeRoot getRootNode() {
    return (SqlTreeRoot)rootNode;
  }

  /**
   * Return the property that is associated with the many. There can only be one
   * per SqlSelect. This can be null.
   */
  STreePropertyAssocMany getManyProperty() {
    return manyProperty;
  }

  STreeProperty[] getEncryptedProps() {
    return encryptedProps;
  }

  /**
   * Return the tables that are joined in this query.
   */
  Set<String> dependentTables() {
    return dependentTables;
  }
}
