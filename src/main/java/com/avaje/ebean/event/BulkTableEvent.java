package com.avaje.ebean.event;

/**
 * The bulk table event.
 * 
 * @author Robin Bygrave
 */
public interface BulkTableEvent {

  /**
   * Return the name of the table that was involved.
   */
  String getTableName();

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
