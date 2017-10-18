package io.ebeaninternal.server.core.bootup;

import io.ebean.config.ServerConfig;
import io.ebeaninternal.server.core.ClassPathScanners;
import org.avaje.classpath.scanner.ClassPathScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Searches for interesting classes such as Entities, Embedded and ScalarTypes.
 */
public class BootupClassPathSearch {

  private static final Logger logger = LoggerFactory.getLogger(BootupClassPathSearch.class);

  private final List<String> packages;

  private final List<ClassPathScanner> scanners;

  /**
   * Search the classPath for the classes we are interested in returning
   * them as BootupClasses.
   */
  public static BootupClasses search(ServerConfig serverConfig) {

    return new BootupClassPathSearch(serverConfig).getBootupClasses();
  }

  private BootupClassPathSearch(ServerConfig serverConfig) {

    // find packages defined in ebean.mf resources
    Set<String> mfPackages = ManifestReader.create(serverConfig.getClassLoadConfig().getClassLoader())
      .read("META-INF/ebean.mf")
      .read("ebean.mf")
      .entityPackages();

    this.packages = DistillPackages.distill(serverConfig.getPackages(), mfPackages);
    this.scanners = ClassPathScanners.find(serverConfig);
  }

  /**
   * Search the classPath for the classes we are interested in.
   */
  private BootupClasses getBootupClasses() {

    try {
      BootupClasses bc = new BootupClasses();

      long st = System.currentTimeMillis();
      for (ClassPathScanner finder : this.scanners) {
        if (packages != null && !packages.isEmpty()) {
          for (String packageName : packages) {
            finder.scanForClasses(packageName, bc);
          }
        } else {
          // scan locally
          finder.scanForClasses("", bc);
        }
      }

      long searchTime = System.currentTimeMillis() - st;
      logger.debug("Classpath search entities[{}] searchTime[{}] in packages[{}]", bc.getEntities().size(), searchTime, packages);
      return bc;

    } catch (Exception ex) {
      throw new RuntimeException("Error in classpath search (looking for entities etc)", ex);
    }
  }

}
