package io.ebeaninternal.server.core;

import io.ebean.config.ServerConfig;
import org.avaje.classpath.scanner.ClassPathScanner;
import org.avaje.classpath.scanner.ClassPathScannerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility to finds and return the list of ClassPathScanner services.
 */
public class ClassPathScanners {

  /**
   * Return the list of ClassPathScanner services using serverConfig service loader.
   */
  public static List<ClassPathScanner> find(ServerConfig serverConfig) {

    List<ClassPathScanner> scanners = new ArrayList<>();
    for (ClassPathScannerFactory factory : serverConfig.serviceLoad(ClassPathScannerFactory.class)) {
      scanners.add(factory.createScanner(serverConfig.getClassLoadConfig().getClassLoader()));
    }
    return scanners;
  }
}
