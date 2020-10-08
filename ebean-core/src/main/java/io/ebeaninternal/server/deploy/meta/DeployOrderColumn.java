package io.ebeaninternal.server.deploy.meta;

import javax.persistence.OrderColumn;

public class DeployOrderColumn {

  /**
   * Logical property name used for order by and available for expression language.
   */
  public static final String LOGICAL_NAME = "orderColumn";

  private final String name;
  private final boolean insertable;
  private final boolean updatable;
  private final boolean nullable;

  public DeployOrderColumn(OrderColumn orderColumn) {
    this.name = orderColumn.name();
    this.insertable = orderColumn.insertable();
    this.updatable = orderColumn.updatable();
    this.nullable = orderColumn.nullable();
  }

  public String getName() {
    return name;
  }

  public boolean isInsertable() {
    return insertable;
  }

  public boolean isUpdatable() {
    return updatable;
  }

  public boolean isNullable() {
    return nullable;
  }
}
