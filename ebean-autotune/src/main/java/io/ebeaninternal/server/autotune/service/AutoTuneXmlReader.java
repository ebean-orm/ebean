package io.ebeaninternal.server.autotune.service;


import io.ebeaninternal.server.autotune.model.Autotune;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
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
  public static Autotune read(File file) {
    try {
      return readFile(file);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  protected static Autotune readFile(File file) throws IOException {
    if (!file.exists()) {
      return new Autotune();
    }
    try (FileInputStream is = new FileInputStream(file)) {
      return read(is);
    }
  }

  /**
   * Read and return a Profiling from an xml document.
   */
  public static Autotune read(InputStream is) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(Autotune.class);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      return (Autotune) unmarshaller.unmarshal(is);
    } catch (JAXBException e) {
      throw new IllegalStateException(e);
    }
  }

}
