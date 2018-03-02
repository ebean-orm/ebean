package io.ebeaninternal.extraddl.model;

import io.ebean.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;

/**
 * Read ExtraDdl from an XML document.
 */
public class ExtraDdlXmlReader {

  private static final Logger logger = LoggerFactory.getLogger(ExtraDdlXmlReader.class);

  /**
   * Return the combined extra DDL that should be run given the platform name.
   */
  public static String buildExtra(String platformName, boolean drops) {

    ExtraDdl read = ExtraDdlXmlReader.read("/extra-ddl.xml");
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

    for (String name : StringHelper.splitNames(platforms)) {
      if (name.toLowerCase().contains(platformName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Read and return a ExtraDdl from an xml document at the given resource path.
   */
  public static ExtraDdl read(String resourcePath) {

    InputStream is = ExtraDdlXmlReader.class.getResourceAsStream(resourcePath);
    if (is == null) {
      // we expect this and check for null
      return null;
    }
    return read(is);
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
