package io.ebeaninternal.server.deploy.parse;

import io.ebean.RawSql;
import io.ebeaninternal.server.deploy.TableJoin;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.deploy.meta.DeployTableJoin;
import io.ebeaninternal.server.query.SqlJoinType;
import io.ebeaninternal.server.rawsql.SpiRawSql;

import java.util.HashMap;

/**
 * Wraps information about a bean during deployment parsing.
 */
public class DeployBeanInfo<T> {

  /**
   * Holds TableJoins for secondary table properties.
   */
  private final HashMap<String, DeployTableJoin> tableJoinMap = new HashMap<>();

  private final DeployUtil util;

  private final DeployBeanDescriptor<T> descriptor;

  /**
   * Create with a DeployUtil and BeanDescriptor.
   */
  public DeployBeanInfo(DeployUtil util, DeployBeanDescriptor<T> descriptor) {
    this.util = util;
    this.descriptor = descriptor;
  }

  @Override
  public String toString() {
    return String.valueOf(descriptor);
  }

  /**
   * Return the BeanDescriptor currently being processed.
   */
  public DeployBeanDescriptor<T> getDescriptor() {
    return descriptor;
  }

  /**
   * Return the DeployUtil we are using.
   */
  public DeployUtil getUtil() {
    return util;
  }

  /**
   * Appropriate TableJoin for a property mapped to a secondary table.
   */
  public DeployTableJoin getTableJoin(String tableName) {

    String key = tableName.toLowerCase();

    DeployTableJoin tableJoin = tableJoinMap.get(key);
    if (tableJoin == null) {
      tableJoin = new DeployTableJoin();
      tableJoin.setTable(tableName);
      tableJoin.setType(SqlJoinType.INNER);
      descriptor.addTableJoin(tableJoin);

      tableJoinMap.put(key, tableJoin);
    }
    return tableJoin;
  }

  /**
   * Add named RawSql from ebean.xml.
   */
  public void addRawSql(String name, RawSql rawSql) {
    descriptor.addRawSql(name, (SpiRawSql)rawSql);
  }

  /**
   * Add the named query.
   */
  public void addNamedQuery(String name, String query) {
    descriptor.addNamedQuery(name, query);
  }

  /**
   * Set that the PK is also a foreign key.
   */
  public void setPrimaryKeyJoin(TableJoin join) {
    descriptor.setPrimaryKeyJoin(join);
  }
}
