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
  public String getTableName();

  /**
   * Return true if rows were inserted.
   */
  public boolean isInsert();

  /**
   * Return true if rows were updated.
   */
  public boolean isUpdate();

  /**
   * Return true if rows were deleted.
   */
  public boolean isDelete();

}
