package io.ebeaninternal.server.core;

import io.ebean.config.ServerConfig;
import org.avaje.classpath.scanner.ClassPathScanner;
import org.avaje.classpath.scanner.ClassPathScannerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Utility to finds and return the list of ClassPathScanner services.
 */
public class ClassPathScanners {

  /**
   * Return the list of ClassPathScanner services using serverConfig service loader.
   */
  public static List<ClassPathScanner> find(ServerConfig serverConfig) {

    List<ClassPathScanner> scanners = new ArrayList<>();

    ServiceLoader<ClassPathScannerFactory> scannerLoader = serverConfig.serviceLoad(ClassPathScannerFactory.class);
    for (ClassPathScannerFactory factory : scannerLoader) {
      ClassPathScanner scanner = factory.createScanner(serverConfig.getClassLoadConfig().getClassLoader());
      scanners.add(scanner);
    }

    return scanners;
  }
}
