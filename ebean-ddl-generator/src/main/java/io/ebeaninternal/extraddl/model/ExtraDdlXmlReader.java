package io.ebeaninternal.extraddl.model;

import io.avaje.applog.AppLog;
import io.ebean.annotation.Platform;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;

import static io.ebeaninternal.api.PlatformMatch.matchPlatform;
import static java.lang.System.Logger.Level.DEBUG;

/**
 * Read ExtraDdl from an XML document.
 */
public class ExtraDdlXmlReader {

  private static final System.Logger logger = AppLog.getLogger(ExtraDdlXmlReader.class);

  /**
   * Return the combined extra DDL that should be run given the platform name.
   */
  public static String buildExtra(Platform platform, boolean drops) {
    return buildExtra(platform, drops, read("/extra-ddl.xml"));
  }

  /**
   * Return any extra DDL for supporting partitioning given the database platform.
   */
  public static String buildPartitioning(Platform platform) {
    return buildExtra(platform, false, readBuiltinTablePartitioning());
  }

  private static String buildExtra(Platform platform, boolean drops, ExtraDdl read) {

    if (read == null) {
      return null;
    }

    StringBuilder sb = new StringBuilder(300);
    for (DdlScript script : read.getDdlScript()) {
      if (script.isDrop() == drops && matchPlatform(platform, script.getPlatforms())) {
        logger.log(DEBUG, "include script {0}", script.getName());
        String value = script.getValue();
        sb.append(value);
        if (value.lastIndexOf(';') == -1) {
          // add statement terminator as we didn't find one
          sb.append(';');
        }
        sb.append('\n');
      }
    }
    return sb.toString();
  }

  /**
   * Read the builtin extra ddl. (Stored procedures, tvp types etc)
   */
  public static ExtraDdl readBuiltin() {
    return read("/io/ebeaninternal/dbmigration/builtin-extra-ddl.xml");
  }

  /**
   * Read the builtin extra ddl to support table partitioning.
   */
  public static ExtraDdl readBuiltinTablePartitioning() {
    return read("/io/ebeaninternal/dbmigration/builtin-extra-ddl-partitioning.xml");
  }

  /**
   * Read the extra ddl.
   */
  public static ExtraDdl read() {
    return read("/extra-ddl.xml");
  }

  /**
   * Read and return a ExtraDdl from an xml document at the given resource path.
   */
  private static ExtraDdl read(String resourcePath) {

    try (InputStream is = ExtraDdlXmlReader.class.getResourceAsStream(resourcePath)) {
      if (is == null) {
        // we expect this and check for null
        return null;
      }
      return read(is);
    } catch (IOException e) {
      throw new IllegalStateException("Error on auto close of " + resourcePath, e);
    }
  }

  /**
   * Read and return a ExtraDdl from an xml document.
   */
  public static ExtraDdl read(InputStream is) {

    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(ExtraDdl.class);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      return (ExtraDdl) unmarshaller.unmarshal(is);

    } catch (JAXBException e) {
      throw new RuntimeException(e);
    }
  }
}
