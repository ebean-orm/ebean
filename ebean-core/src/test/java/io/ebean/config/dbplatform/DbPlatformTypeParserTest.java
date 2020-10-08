package io.ebean.config.dbplatform;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DbPlatformTypeParserTest {

  @Test
  public void parse_text() throws Exception {

    DbPlatformType type = DbPlatformTypeParser.parse("text");

    assertEquals(type.getName(), "text");
    assertEquals(type.getDefaultLength(), 0);
    assertEquals(type.getDefaultScale(), 0);

    assertEquals(type.renderType(0, 0), "text");
    assertEquals(type.renderType(40, 0), "text");
  }

  @Test
  public void parse_varchar20() throws Exception {

    DbPlatformType type = DbPlatformTypeParser.parse("varchar(20)");

    assertEquals(type.getName(), "varchar");
    assertEquals(type.getDefaultLength(), 20);
    assertEquals(type.getDefaultScale(), 0);

    assertEquals(type.renderType(0, 0), "varchar(20)");
    assertEquals(type.renderType(40, 0), "varchar(40)");
  }

  @Test
  public void parse_decimal_18_6() throws Exception {

    DbPlatformType type = DbPlatformTypeParser.parse("decimal(18,6)");

    assertEquals(type.getName(), "decimal");
    assertEquals(type.getDefaultLength(), 18);
    assertEquals(type.getDefaultScale(), 6);
    assertEquals(type.renderType(0, 0), "decimal(18,6)");
  }

  @Test
  public void parse_something() throws Exception {

    DbPlatformType type = DbPlatformTypeParser.parse("something(asd,6)");

    assertEquals(type.getName(), "something(asd,6)");
    assertEquals(type.getDefaultLength(), 0);
    assertEquals(type.getDefaultScale(), 0);
    assertEquals(type.renderType(0, 0), "something(asd,6)");
  }

  @Test
  public void parse_invalid() throws Exception {

    DbPlatformType type = DbPlatformTypeParser.parse("something(asd");

    assertEquals(type.getName(), "something(asd");
    assertEquals(type.getDefaultLength(), 0);
    assertEquals(type.getDefaultScale(), 0);
    assertEquals(type.renderType(0, 0), "something(asd");
  }
}
