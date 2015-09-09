package com.avaje.ebeaninternal.server.autofetch.service;


import com.avaje.ebeaninternal.server.autofetch.model.Autotune;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads a profiling xml document.
 */
public class AutoTuneXmlReader {

  /**
   * Read and return a Profiling from an xml file.
   */
  public Autotune read(File file) {

    try {
      return readFile(file);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  protected Autotune readFile(File file) throws IOException {
    if (!file.exists()) {
      return new Autotune();
    }
    FileInputStream is = new FileInputStream(file);
    try {
      return read(is);
    } finally {
      is.close();
    }
  }

  /**
   * Read and return a Profiling from an xml document.
   */
  public Autotune read(InputStream is) {

    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(Autotune.class);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      return (Autotune) unmarshaller.unmarshal(is);
    } catch (JAXBException e) {
      throw new IllegalStateException(e);
    }
  }

}
