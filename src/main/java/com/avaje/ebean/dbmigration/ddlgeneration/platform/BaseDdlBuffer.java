package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.dbmigration.ddlgeneration.DdlBuffer;
import com.avaje.ebean.dbmigration.model.MConfiguration;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Base implementation of DdlBuffer using an underlying writer.
 */
public class BaseDdlBuffer implements DdlBuffer {

  protected final StringWriter writer;

  protected final MConfiguration configuration;

  public BaseDdlBuffer(MConfiguration configuration) {
    this.configuration = configuration;
    this.writer = new StringWriter();
  }

  @Override
  public MConfiguration getConfiguration() {
    return configuration;
  }

  @Override
  public boolean isEmpty() {
    return writer.getBuffer().length() == 0;
  }

  @Override
  public DdlBuffer appendWithSpace(String foreignKeyRestrict) throws IOException {
    if (foreignKeyRestrict != null && !foreignKeyRestrict.isEmpty()) {
      writer.append(" ").append(foreignKeyRestrict);
    }
    return this;
  }

  @Override
  public DdlBuffer append(String content) throws IOException {
    writer.append(content);
    return this;
  }

  @Override
  public DdlBuffer append(String content, int space) throws IOException {
    writer.append(content);
    appendSpace(space, content);
    return this;
  }

  protected void appendSpace(int max, String content) throws IOException {
    int space = max - content.length();
    if (space > 0) {
      for (int i = 0; i < space; i++) {
        append(" ");
      }
    }
    append(" ");
  }

  @Override
  public DdlBuffer endOfStatement() throws IOException {
    writer.append(";\n");
    return this;
  }

  /**
   * Used to demarcate the end of a series of statements.
   * This should be just whitespace or a sql comment.
   */
  @Override
  public DdlBuffer end() throws IOException {
    writer.append("\n");
    return this;
  }

  @Override
  public DdlBuffer newLine() throws IOException {
    writer.append("\n");
    return this;
  }

  public String getBuffer() {
    return writer.toString();
  }
}
