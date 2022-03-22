package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.ebeaninternal.dbmigration.ddlgeneration.DdlAlterTable;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;

/**
 * Contains alter statements per table.
 * 
 * @author Roland Praml, FOCONIS AG
 */
public class BaseAlterTableWrite implements DdlAlterTable {

  protected static final String RAW_OPERATION = "$RAW";

  protected final PlatformDdl platformDdl;

  public class AlterCmd {
    // the command (e.g. "alter", "modify"
    private final String operation;
    // the affected column (note: each column can only be altered once on MariaDB)
    private final String column;

    private final DdlBuffer alternationBuffer = new BaseDdlBuffer() {
      @Override
      public DdlBuffer endOfStatement() {
        throw new UnsupportedOperationException();
      };
    };

    protected AlterCmd(String operation, String column) {
      this.operation = operation;
      this.column = column;
    }

    public AlterCmd append(String content) {
      alternationBuffer.append(content);
      return this;
    }

    public String getOperation() {
      return operation;
    }

    public String getColumn() {
      return column;
    }

    public String getAlternation() {
      return alternationBuffer.getBuffer();
    }

    protected void write(Appendable target) throws IOException {
      if (operation.equals(RAW_OPERATION)) {
        // this is a raw command. e.g. an USP call. Must be done in the correct order
        // of all alter commands
        target.append(getAlternation());
      } else {
        target.append("alter table ").append(platformDdl.quote(tableName)).append(' ').append(operation);
        if (column != null) {
          target.append(' ').append(platformDdl.quote(column));
        }
        if (!getAlternation().isEmpty()) {
          target.append(' ').append(getAlternation());
        }
      }
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      try {
        write(sb);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return sb.toString();
    }
  }

  private final String tableName;

  private List<AlterCmd> cmds = new ArrayList<>();

  private boolean historyHandled;

  public BaseAlterTableWrite(String tableName, PlatformDdl platformDdl) {
    this.tableName = tableName;
    this.platformDdl = platformDdl;
  }

  public String tableName() {
    return tableName;
  }

  protected AlterCmd newRawCommand(String sql) {
    AlterCmd cmd = new AlterCmd(RAW_OPERATION, null);
    cmd.alternationBuffer.append(sql);
    return cmd;
  }

  public AlterCmd newOperation(String operation, String column) {
    return new AlterCmd(operation, column);
  }

  /**
   * Adds a statement. The statement is prefixed with "alter table TABLENAME" and may be batched, if platform supports this. The
   * returned StringBuilder can be used, to complete the statement
   */
  @Override
  public DdlBuffer append(String operation, String column) {
    AlterCmd cmd = new AlterCmd(operation, column);
    cmds.add(cmd);
    return cmd.alternationBuffer;
  }

  @Override
  public DdlBuffer raw(String sql) {
    AlterCmd cmd = newRawCommand(sql);
    cmds.add(cmd);
    return cmd.alternationBuffer;
  }

  /**
   * Method can be overwritten to return a new list of commands. The given list must not be modified, but a new command list with
   * modified commands can be returned (e.g. to handle DB2 reorg or special syntax in Hana)
   */
  protected List<AlterCmd> postProcessCommands(List<AlterCmd> cmds) {
    return cmds;
  }

  /**
   * Writes the DDL to <code>target</code>.
   */
  @Override
  public void write(Appendable target) throws IOException {
    for (AlterCmd cmd : postProcessCommands(Collections.unmodifiableList(cmds))) {
      cmd.write(target);
      target.append(";\n");
    }
  }

  @Override
  public boolean isHistoryHandled() {
    return historyHandled;
  }

  @Override
  public void setHistoryHandled() {
    historyHandled = true;
  }
}
