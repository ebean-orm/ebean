package io.ebeaninternal.dbmigration.migrationreader;


import io.ebeaninternal.dbmigration.migration.Migration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Simple writer for output of the Migration/ChangeSet as an XML document.
 */
public class MigrationXmlWriter {

  private final String comment;

  public MigrationXmlWriter(String comment) {
    this.comment = comment;
  }

  /**
   * Write a Migration as a standalone XML document to a file.
   */
  public void write(Migration migration, File file) {

    try (FileWriter writer = new FileWriter(file)) {

      writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
      if (comment != null) {
        writer.write("<!-- ");
        writer.write(comment);
        writer.write(" -->\n");
      }

      JAXBContext jaxbContext = JAXBContext.newInstance(Migration.class);
      Marshaller marshaller = jaxbContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

      marshaller.marshal(migration, writer);

    } catch (IOException | JAXBException e) {
      throw new RuntimeException(e);
    }
  }

}
