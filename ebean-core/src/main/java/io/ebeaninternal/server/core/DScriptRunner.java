package io.ebeaninternal.server.core;

import io.ebean.ScriptRunner;
import io.ebean.ddlrunner.DdlRunner;
import io.ebean.ddlrunner.ScriptTransform;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.util.UrlHelper;

import javax.persistence.PersistenceException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

class DScriptRunner implements ScriptRunner {

  private static final String NEWLINE = "\n";

  private final SpiEbeanServer server;
  private final String platformName;

  DScriptRunner(SpiEbeanServer server) {
    this.server = server;
    this.platformName = this.server.getDatabasePlatform().getPlatform().base().name();
  }

  @Override
  public void run(String path) {
    run(path, null);
  }

  @Override
  public void run(String path, Map<String, String> placeholderMap) {
    run(this.getClass().getResource(path), path, placeholderMap);
  }

  @Override
  public void run(URL resource) {
    run(resource, null, null);
  }

  @Override
  public void run(URL resource, Map<String, String> placeholderMap) {
    run(resource, null, placeholderMap);
  }

  private void run(URL resource, String scriptName, Map<String, String> placeholderMap) {
    if (resource == null) {
      throw new IllegalArgumentException("resource is null?");
    }
    if (scriptName == null) {
      scriptName = resource.getFile();
    }

    String content = content(resource);
    runScript(content, scriptName, placeholderMap, false);
  }

  private String content(URL resource) {
    if (resource == null) {
      throw new IllegalArgumentException("resource is null?");
    }

    try (InputStream inputStream = UrlHelper.openNoCache(resource)) {
      return readContent(new InputStreamReader(inputStream));

    } catch (IOException e) {
      throw new PersistenceException("Failed to read script content", e);
    }
  }

  @Override
  public void runScript(String name, String content, boolean useAutoCommit) {
    runScript(content, name, null, useAutoCommit);
  }

  /**
   * Execute all the DDL statements in the script.
   */
  private void runScript(String content, String scriptName, Map<String, String> placeholderMap, boolean useAutoCommit) {
    try {
      if (placeholderMap != null) {
        content = ScriptTransform.build(null, placeholderMap).transform(content);
      }

      try (Connection connection = obtainConnection()) {
        DdlRunner runner = new DdlRunner(useAutoCommit, scriptName, platformName);
        runner.runAll(content, connection);
        connection.commit();
        runner.runNonTransactional(connection);
      }

    } catch (SQLException e) {
      throw new PersistenceException("Failed to run script", e);
    }
  }

  private Connection obtainConnection() {
    try {
      return server.getDataSource().getConnection();
    } catch (SQLException e) {
      throw new PersistenceException("Failed to obtain connection to run script", e);
    }
  }

  private String readContent(Reader reader) throws IOException {

    StringBuilder buf = new StringBuilder();
    try (LineNumberReader lineReader = new LineNumberReader(reader)) {
      String line;
      while ((line = lineReader.readLine()) != null) {
        buf.append(line).append(NEWLINE);
      }
      return buf.toString();
    }
  }

}
