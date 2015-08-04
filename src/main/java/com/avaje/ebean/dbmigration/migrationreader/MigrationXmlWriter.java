package com.avaje.ebean.dbmigration.migrationreader;


import com.avaje.ebean.dbmigration.migration.Migration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;

/**
 * Simple writer for output of the Migration/ChangeSet as an XML document.
 */
public class MigrationXmlWriter {

  /**
   * Write a Migration to a file as an xml document to the file.
   */
  public void write(Migration migration, File file) {

    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(Migration.class);
      Marshaller marshaller = jaxbContext.createMarshaller();
      marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(migration, file);

    } catch (JAXBException e) {
      throw new RuntimeException(e);
    }
  }

}
