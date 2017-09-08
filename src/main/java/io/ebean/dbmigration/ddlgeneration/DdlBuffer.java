package io.ebean.dbmigration.ddlgeneration;

import java.io.IOException;

/**
 * Buffer to append generated DDL to.
 */
public interface DdlBuffer {

  /**
   * Return true if the buffer is empty.
   */
  boolean isEmpty();

  /**
   * Append DDL content to the buffer.
   */
  DdlBuffer append(String content) throws IOException;

  /**
   * Append DDL content to the buffer with space padding.
   */
  DdlBuffer append(String type, int space) throws IOException;

  /**
   * Append new line character to the buffer.
   */
  DdlBuffer newLine() throws IOException;

  /**
   * Append the end of statement content.
   */
  DdlBuffer endOfStatement() throws IOException;

  /**
   * End of a change - add some whitespace.
   */
  DdlBuffer end() throws IOException;

  /**
   * Return the buffer content.
   */
  String getBuffer();

}
