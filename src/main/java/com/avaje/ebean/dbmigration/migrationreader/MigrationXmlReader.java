package com.avaje.ebean.dbmigration.migrationreader;


import com.avaje.ebean.dbmigration.migration.Migration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;

/**
 * Reads a migration xml document returning the Migration.
 */
public class MigrationXmlReader {

  private static final MigrationXmlReader INSTANCE = new MigrationXmlReader();

  /**
   * Read and return a Migration from an xml document at the given resource path.
   */
  public static Migration readMaybe(String resourcePath) {

    InputStream is = MigrationXmlReader.class.getResourceAsStream(resourcePath);
    if (is == null) {
      return null;
    }

    return INSTANCE.read(is);
  }

  /**
   * Read and return a Migration from an xml document at the given resource path.
   */
  public static Migration read(String resourcePath) {

    InputStream is = MigrationXmlReader.class.getResourceAsStream(resourcePath);
    if (is == null) {
      throw new IllegalArgumentException("No resource found for path [" + resourcePath + "]");
    }

    return INSTANCE.read(is);
  }

  /**
   * Read and return a Migration from an xml document.
   */
  public Migration read(InputStream is) {

    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(Migration.class);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      return (Migration) unmarshaller.unmarshal(is);

    } catch (JAXBException e) {
      throw new RuntimeException(e);
    }
  }

}
