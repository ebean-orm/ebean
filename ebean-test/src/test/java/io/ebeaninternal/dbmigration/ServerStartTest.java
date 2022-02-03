package io.ebeaninternal.dbmigration;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;

public class ServerStartTest {

  
  @Test
  void testServerStartAndMigrate() throws Exception {
    DatabaseFactory.create("db2-migration").shutdown();
  }
}
