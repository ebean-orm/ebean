package io.ebeaninternal.dbmigration.ddlgeneration;

import java.io.IOException;

/**
 * Object that represents an Alter Table statements. Table alters are grouped together by tableName in DDL and can be extended by
 * a ddl-specific handler (e.g. doing a reorg for DB2 or implement special grouping for Hana). There is one instance per table.
 * 
 * @author TODO Roland Praml, FOCONIS AG
 */
public interface DdlAlterTable {

  /**
   * Writes the alter table statements to <code>target</code>
   */
  void write(Appendable target) throws IOException;

  /**
   * Adds an alter table command for given column. When you invoke<br>
   * <code>alterTable(writer, "my_table").add("alter column","my_column).append("type integer")</code> the resulting DDL (in
   * standard implementation) would be<br>
   * <code>alter table my_table alter column my_column type integer</code>
   * 
   * @return a DdlBuffer, which can be used for further appends. Note you MUST NOT call <code>.endOfStatement()</code> on this
   *         buffer.
   */
  DdlBuffer append(String operation, String columnName);

  /**
   * Adds a raw command. This is mainly used for executing user stored procedures.
   */
  DdlBuffer raw(String string);

  /**
   * Flag that detects if history DDL (switching on) is handled for this table.
   */
  boolean isHistoryHandled();

  /**
   * Sets the history handled flag for this table.
   */
  void setHistoryHandled();


}
