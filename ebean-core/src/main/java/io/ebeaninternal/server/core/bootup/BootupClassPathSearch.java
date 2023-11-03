package io.ebeaninternal.server.core.bootup;

import io.avaje.classpath.scanner.ClassPathScanner;
import io.ebean.DatabaseBuilder;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.server.core.ClassPathScanners;

import java.util.List;
import java.util.Set;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * Searches for interesting classes such as Entities, Embedded and ScalarTypes.
 */
public class BootupClassPathSearch {

  private static final System.Logger log = CoreLog.internal;

  private final List<String> packages;
  private final List<ClassPathScanner> scanners;

  /**
   * Search the classPath for the classes we are interested in returning
   * them as BootupClasses.
   */
  public static BootupClasses search(DatabaseBuilder.Settings config) {
    return new BootupClassPathSearch(config).getBootupClasses();
  }

  private BootupClassPathSearch(DatabaseBuilder.Settings config) {
    // find packages defined in ebean.mf resources
    Set<String> mfPackages = ManifestReader.create(config.getClassLoadConfig().getClassLoader())
      .read("META-INF/ebean.mf")
      .read("ebean.mf")
      .entityPackages();

    this.packages = DistillPackages.distill(config.getPackages(), mfPackages);
    this.scanners = ClassPathScanners.find(config);
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
      log.log(DEBUG, "Classpath search entities[{0}] searchTime[{1}] in packages[{2}]", bc.getEntities().size(), searchTime, packages);
      return bc;

    } catch (Exception ex) {
      throw new RuntimeException("Error in classpath search (looking for entities etc)", ex);
    }
  }

}
