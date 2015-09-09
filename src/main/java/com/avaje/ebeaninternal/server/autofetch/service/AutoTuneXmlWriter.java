package com.avaje.ebeaninternal.server.autofetch.service;


import com.avaje.ebeaninternal.server.autofetch.model.Autotune;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;

/**
 * Simple writer for output of the AutoTune Profiling as an XML document.
 */
public class AutoTuneXmlWriter {

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
