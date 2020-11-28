package io.ebeaninternal.server.type;

import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class ScalarTypePathTest {
  private static final String TEMP_PATH = new File("/tmp").getAbsolutePath();

  private ScalarTypePath type = new ScalarTypePath();

  @Test
  public void convertFromDbString() throws Exception {

    Path path = Paths.get(TEMP_PATH);

    String asString = type.convertToDbString(path); // "/tmp" will be converted to "file://c:/tmp" on windows
    Path converted = type.convertFromDbString(asString);

    assertEquals(path, converted);
  }

  @Test
  public void formatAndParse() throws Exception {

    Path path = Paths.get(TEMP_PATH);

    String asString = type.formatValue(path);
    Path converted = type.parse(asString);

    assertEquals(path, converted);
  }

}
