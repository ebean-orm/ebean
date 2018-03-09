package io.ebeaninternal.api;

import io.ebean.Update;

/**
 * Internal extension to the Update interface.
 */
public interface SpiUpdate<T> extends Update<T> {

  /**
   * The type of the update request.
   */
  enum OrmUpdateType {
    INSERT {
      @Override
      public String toString() {
        return "Insert";
      }
    },
    UPDATE {
      @Override
      public String toString() {
        return "Update";
      }
    },
    DELETE {
      @Override
      public String toString() {
        return "Delete";
      }
    },
    UNKNOWN {
      @Override
      public String toString() {
        return "Unknown";
      }

    }
  }

  /**
   * Return the type of bean being updated.
   */
  Class<?> getBeanType();

  /**
   * Return the label (for metrics collection).
   */
  String getLabel();

  /**
   * Return the type of this - insert, update or delete.
   */
  OrmUpdateType getOrmUpdateType();

  /**
   * Return the name of the table being modified.
   */
  String getBaseTable();

  /**
   * Return the update statement. This could be either sql or an orm update with bean types and property names.
   */
  String getUpdateStatement();

  /**
   * Return the timeout in seconds.
   */
  int getTimeout();

  /**
   * Return true if the cache should be notified to invalidate objects.
   */
  boolean isNotifyCache();

  /**
   * Return the bind parameters.
   */
  BindParams getBindParams();

  /**
   * Set the generated sql used.
   */
  void setGeneratedSql(String sql);
}
