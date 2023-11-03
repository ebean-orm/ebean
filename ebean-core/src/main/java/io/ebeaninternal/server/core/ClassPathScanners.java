package io.ebeaninternal.server.core;

import io.avaje.classpath.scanner.ClassPathScanner;
import io.avaje.classpath.scanner.ClassPathScannerFactory;
import io.ebean.DatabaseBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Utility to finds and return the list of ClassPathScanner services.
 */
public class ClassPathScanners {

  /**
   * Return the list of ClassPathScanner services using DatabaseConfig service loader.
   */
  public static List<ClassPathScanner> find(DatabaseBuilder config) {
    List<ClassPathScanner> scanners = new ArrayList<>();
    for (ClassPathScannerFactory factory : ServiceLoader.load(ClassPathScannerFactory.class)) {
      scanners.add(factory.createScanner(config.getClassLoadConfig().getClassLoader()));
    }
    return scanners;
  }
}
