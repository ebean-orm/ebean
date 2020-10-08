package io.ebeaninternal.server.autotune.service;


import io.ebeaninternal.server.autotune.model.Autotune;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Simple writer for output of the AutoTune Profiling as an XML document.
 */
public class AutoTuneXmlWriter {

  /**
   * Return 'now' as a string to second precision.
   */
  public static String now() {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
    return df.format(new Date());
  }

  /**
   * Write the document as xml file with the given prefix.
   */
  public void write(Autotune document, String fileName, boolean withNow) {

    SortAutoTuneDocument.sort(document);

    if (withNow) {
      fileName += "-" + now() + ".xml";
    }

    // write the file with serverName and now suffix as we can output the profiling many times
    write(document, new File(fileName));
  }

  /**
   * Write Profiling to a file as xml.
   */
  public void write(Autotune profiling, File file) {

    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(Autotune.class);
      Marshaller marshaller = jaxbContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(profiling, file);

    } catch (JAXBException e) {
      throw new RuntimeException(e);
    }
  }

}
