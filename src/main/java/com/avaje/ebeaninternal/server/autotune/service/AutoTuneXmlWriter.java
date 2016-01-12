package com.avaje.ebeaninternal.server.autotune.service;


import com.avaje.ebeaninternal.server.autotune.model.Autotune;

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
   * Write the document as xml file with the given prefix.
   */
  public void write(Autotune document, String filePrefix) {

    SortAutoTuneDocument.sort(document);

    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
    String now = df.format(new Date());

    // write the file with serverName and now suffix as we can output the profiling many times
    File file = new File(filePrefix + "-" + now + ".xml");
    write(document, file);
  }

  /**
   * Write Profiling to a file as xml.
   */
  public void write(Autotune profiling, File file) {

    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(Autotune.class);
      Marshaller marshaller = jaxbContext.createMarshaller();
      marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(profiling, file);

    } catch (JAXBException e) {
      throw new RuntimeException(e);
    }
  }

}
