package io.ebeaninternal.server.autotune.service;

import io.ebeaninternal.server.autotune.model.Autotune;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class AutoTuneXmlReaderTest {

  @Test
  public void read_file() throws Exception {

    File testFile = new File("src/test/resources/autotune/test-autotune.xml");

    Autotune tuneInfo = AutoTuneXmlReader.read(testFile);
    assertThat(tuneInfo.getOrigin()).isNotEmpty();
  }

  @Test
  public void read_inputStream() throws Exception {

    InputStream is = getClass().getResourceAsStream("/autotune/test-autotune.xml");

    Autotune tuneInfo = AutoTuneXmlReader.read(is);
    assertThat(tuneInfo.getOrigin()).isNotEmpty();
  }

}
