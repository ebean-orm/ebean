package io.ebeaninternal.extraddl.model;

import io.ebean.annotation.Platform;
import io.ebean.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;

/**
 * Read ExtraDdl from an XML document.
 */
public class ExtraDdlXmlReader {

  private static final Logger logger = LoggerFactory.getLogger(ExtraDdlXmlReader.class);

  private static final String SQLSERVER = Platform.SQLSERVER.name().toLowerCase();

  /**
   * Return the combined extra DDL that should be run given the platform name.
   */
  public static String buildExtra(String platformName, boolean drops) {

    ExtraDdl read = ExtraDdlXmlReader.read("/extra-ddl.xml");
    return buildExtra(platformName, drops, read);
  }

  /**
   * Return any extra DDL for supporting partitioning given the database platform.
   */
  public static String buildPartitioning(String platformName) {
    return buildExtra(platformName, false, readBuiltinTablePartitioning());
  }

  private static String buildExtra(String platformName, boolean drops, ExtraDdl read) {

    if (read == null) {
      return null;
    }

    StringBuilder sb = new StringBuilder(300);
    for (DdlScript script : read.getDdlScript()) {
      if (script.isDrop() == drops && matchPlatform(platformName, script.getPlatforms())) {
        logger.debug("include script {}", script.getName());
        String value = script.getValue();
        sb.append(value);
        if (value.lastIndexOf(';') == -1) {
          // add statement terminator as we didn't find one
          sb.append(";");
        }
        sb.append("\n");
      }
    }
    return sb.toString();
  }

  /**
   * Return true if the script platforms is a match/supported for the given platform.
   *
   * @param platformName The database platform we are generating/running DDL for
   * @param platforms    The platforms (comma delimited) this script should run for
   */
  public static boolean matchPlatform(String platformName, String platforms) {
    if (platforms == null || platforms.trim().isEmpty()) {
      return true;
    }

    String genericMatch = genericPlatformMatch(platformName);

    for (String name : StringHelper.splitNames(platforms)) {
      if (name.toLowerCase().contains(platformName)) {
        return true;
      } else if (genericMatch != null && genericMatch.equals(name.toLowerCase())) {
        // allow sqlserver ... to match sqlserver17 and sqlserver16 platforms
        return true;
      }
    }
    return false;
  }

  /**
   * Return a "generic" platform name that can be used. e.g. sqlserver17 -> sqlserver.
   */
  private static String genericPlatformMatch(String platformName) {
    switch (platformName) {
      case "sqlserver17":
        return SQLSERVER;
      case "sqlserver16":
        return SQLSERVER;
      default:
        return null;
    }
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
