package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean holding comments relating to a history table that needs to have it's
 * associated triggers/function updated due to columns added/dropped/included
 * or excluded.
 */
public class HistoryTableUpdate {

  private final String baseTable;

  private final List<String> comments = new ArrayList<String>();

  /**
   * Construct with a given base table name.
   */
  public HistoryTableUpdate(String baseTable) {
    this.baseTable = baseTable;
  }

  /**
   * Add a comment for column added, dropped, included or excluded.
   */
  public void addComment(String comment) {
    comments.add(comment);
  }

  /**
   * Return the base table name.
   */
  public String getBaseTable() {
    return baseTable;
  }

  /**
   * Return the comments.
   */
  public List<String> getComments() {
    return comments;
  }
}
