package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.model.MConfiguration;


/**
 * Base implementation of DdlBuffer using an underlying writer.
 */
public class BaseDdlBuffer implements DdlBuffer {

  protected final StringBuilder writer;

  public BaseDdlBuffer() {
    this.writer = new StringBuilder();
  }

  @Override
  public boolean isEmpty() {
    return writer.length() == 0;
  }

  @Override
  public DdlBuffer appendWithSpace(String content) {
    if (content != null && !content.isEmpty()) {
      writer.append(" ").append(content);
    }
    return this;
  }

  @Override
  public DdlBuffer appendStatement(String content) {
    if (content != null && !content.isEmpty()) {
      writer.append(content);
      endOfStatement();
    }
    return this;
  }

  @Override
  public DdlBuffer append(String content) {
    writer.append(content);
    return this;
  }

  @Override
  public DdlBuffer append(String content, int space) {
    writer.append(content);
    appendSpace(space, content);
    return this;
  }

  protected void appendSpace(int max, String content) {
    int space = max - content.length();
    if (space > 0) {
      for (int i = 0; i < space; i++) {
        append(" ");
      }
    }
    append(" ");
  }

  @Override
  public DdlBuffer endOfStatement() {
    writer.append(";\n");
    return this;
  }

  /**
   * Used to demarcate the end of a series of statements.
   * This should be just whitespace or a sql comment.
   */
  @Override
  public DdlBuffer end() {
    if (!isEmpty()) {
      writer.append("\n");
    }
    return this;
  }

  @Override
  public DdlBuffer newLine() {
    writer.append("\n");
    return this;
  }

  @Override
  public String getBuffer() {
    return writer.toString();
  }
}
