package io.ebean.event;

/**
 * The bulk table event.
 */
public interface BulkTableEvent {

  /**
   * Return the name of the table that was involved.
   */
  String tableName();

  /**
   * Deprecated migrate to tableName().
   */
  @Deprecated
  default String getTableName() {
    return tableName();
  }

  /**
   * Return true if rows were inserted.
   */
  boolean isInsert();

  /**
   * Return true if rows were updated.
   */
  boolean isUpdate();

  /**
   * Return true if rows were deleted.
   */
  boolean isDelete();

}
