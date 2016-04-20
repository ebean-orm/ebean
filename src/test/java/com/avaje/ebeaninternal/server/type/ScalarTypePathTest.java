package com.avaje.ebeaninternal.server.type;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class ScalarTypePathTest {

  private ScalarTypePath type = new ScalarTypePath();
  
  @Test
  public void convertFromDbString() throws Exception {

    Path path = Paths.get(".");

    String asString = type.convertToDbString(path);
    Path converted = type.convertFromDbString(asString);

    assertEquals(path, converted);
  }

  @Test
  public void formatAndParse() throws Exception {

    Path path = Paths.get(".");

    String asString = type.formatValue(path);
    Path converted = type.parse(asString);

    assertEquals(path, converted);
  }

}