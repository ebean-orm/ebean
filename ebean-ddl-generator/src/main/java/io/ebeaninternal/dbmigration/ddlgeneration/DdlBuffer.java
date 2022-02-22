package io.ebeaninternal.dbmigration.ddlgeneration;

/**
 * Buffer to append generated DDL to.
 */
public interface DdlBuffer {

  /**
   * Return true if the buffer is empty.
   */
  boolean isEmpty();

  /**
   * Append a statement allowing for null or empty statements.
   */
  DdlBuffer appendStatement(String content);

  /**
   * Append DDL content to the buffer.
   */
  DdlBuffer append(String content);

  /**
   * Append DDL content to the buffer with space padding.
   */
  DdlBuffer append(String type, int space);

  /**
   * Append a value that is potentially null or empty and proceed it with a space if so.
   */
  DdlBuffer appendWithSpace(String foreignKeyRestrict);

  /**
   * Append new line character to the buffer.
   */
  DdlBuffer newLine();

  /**
   * Append the end of statement content.
   */
  DdlBuffer endOfStatement();

  /**
   * End of a change - add some whitespace.
   */
  DdlBuffer end();

  /**
   * Return the buffer content.
   */
  String getBuffer();

}
