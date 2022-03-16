package io.ebeaninternal.server.core;

import io.ebean.ScriptRunner;
import io.ebean.ddlrunner.DdlRunner;
import io.ebean.ddlrunner.ScriptTransform;
import io.ebean.util.IOUtils;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.util.UrlHelper;

import javax.persistence.PersistenceException;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static java.util.Objects.requireNonNull;

final class DScriptRunner implements ScriptRunner {

  private static final String NEWLINE = "\n";

  private final SpiEbeanServer server;
  private final String platformName;

  DScriptRunner(SpiEbeanServer server) {
    this.server = server;
    this.platformName = this.server.platform().base().name();
  }

  @Override
  public void run(String resourcePath) {
    run(resourcePath, null);
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

  @Override
  public void run(Path file) {
    run(file, null);
  }

  @Override
  public void run(Path file, Map<String, String> placeholderMap) {
    requireNonNull(file);
    String scriptName = file.toFile().getName();
    String content = fileContent(file);
    runScript(content, scriptName, placeholderMap, false);
  }

  private void run(URL resource, String scriptName, Map<String, String> placeholderMap) {
    requireNonNull(resource);
    if (scriptName == null) {
      scriptName = resource.getFile();
    }
    String content = content(resource);
    runScript(content, scriptName, placeholderMap, false);
  }

  private String fileContent(Path file) {
    try (InputStream inputStream = new FileInputStream(file.toFile());
         Reader reader = IOUtils.newReader(inputStream)) {
      return readContent(reader);
    } catch (IOException e) {
      throw new PersistenceException("Failed to read script content", e);
    }
  }

  private String content(URL resource) {
    requireNonNull(resource);
    try (InputStream inputStream = UrlHelper.openNoCache(resource);
         Reader reader = IOUtils.newReader(inputStream)) {
      return readContent(reader);
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
      if (placeholderMap != null && !placeholderMap.isEmpty()) {
        content = ScriptTransform.build(null, placeholderMap).transform(content);
      }
      try (Connection connection = obtainConnection()) {
        DdlRunner runner = new DdlRunner(useAutoCommit, scriptName, platformName);
        runner.runAll(content, connection);
        if (!connection.getAutoCommit()) {
          connection.commit();
        }
        runner.runNonTransactional(connection);
      }

    } catch (SQLException e) {
      throw new PersistenceException("Failed to run script", e);
    }
  }

  private Connection obtainConnection() {
    try {
      return server.dataSource().getConnection();
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
