package io.ebeaninternal.server.rawsql;

import io.ebeaninternal.server.rawsql.SpiRawSql.ColumnMapping;
import io.ebeaninternal.server.rawsql.SpiRawSql.ColumnMapping.Column;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestRawSqlColumnParsing {

  @Test
  public void test_simple() {

    ColumnMapping columnMapping = DRawSqlColumnsParser.parse("a,b,c");
    Map<String, Column> mapping = columnMapping.mapping();
    Column c = mapping.get("a");

    assertEquals("a", c.getDbColumn());
    assertEquals(0, c.getIndexPos());
    assertEquals("a", c.getPropertyName());

    c = mapping.get("b");
    assertEquals("b", c.getDbColumn());
    assertEquals(1, c.getIndexPos());
    assertEquals("b", c.getPropertyName());

    c = mapping.get("c");
    assertEquals("c", c.getDbColumn());
    assertEquals(2, c.getIndexPos());
    assertEquals("c", c.getPropertyName());

  }

  @Test
  public void test_simpleWithSpacing() {

    ColumnMapping columnMapping = DRawSqlColumnsParser.parse(" a  ,  b    ,  c   ");
    Map<String, Column> mapping = columnMapping.mapping();
    Column c = mapping.get("a");

    assertEquals("a", c.getDbColumn());
    assertEquals(0, c.getIndexPos());
    assertEquals("a", c.getPropertyName());

    c = mapping.get("b");
    assertEquals("b", c.getDbColumn());
    assertEquals(1, c.getIndexPos());
    assertEquals("b", c.getPropertyName());

    c = mapping.get("c");
    assertEquals("c", c.getDbColumn());
    assertEquals(2, c.getIndexPos());
    assertEquals("c", c.getPropertyName());

  }

  @Test
  public void test_withAlias() {

    ColumnMapping columnMapping = DRawSqlColumnsParser.parse("a a0,b    b1, c  c2 ,   d    d3  , e  e4 ");
    Map<String, Column> mapping = columnMapping.mapping();

    assertEquals(5, mapping.size());

    Column c = mapping.get("a0");

    assertEquals("a", c.getDbColumn());
    assertEquals(0, c.getIndexPos());
    assertEquals("a0", c.getPropertyName());

    c = mapping.get("b1");
    assertEquals("b", c.getDbColumn());
    assertEquals(1, c.getIndexPos());
    assertEquals("b1", c.getPropertyName());

    c = mapping.get("c2");
    assertEquals("c", c.getDbColumn());
    assertEquals(2, c.getIndexPos());
    assertEquals("c2", c.getPropertyName());

    c = mapping.get("d3");
    assertEquals("d", c.getDbColumn());
    assertEquals(3, c.getIndexPos());
    assertEquals("d3", c.getPropertyName());


    c = mapping.get("e4");
    assertEquals("e", c.getDbColumn());
    assertEquals(4, c.getIndexPos());
    assertEquals("e4", c.getPropertyName());

  }

  @Test
  public void test_withDatabaseFunction() {

    ColumnMapping columnMapping = DRawSqlColumnsParser.parse("a a0,b    b1, MONTH(MAKEDATE(2015, 241)) m2 ,   d    d3  , e  e4 ");
    Map<String, Column> mapping = columnMapping.mapping();

    assertEquals(5, mapping.size());

    Column c = mapping.get("a0");

    assertEquals("a", c.getDbColumn());
    assertEquals(0, c.getIndexPos());
    assertEquals("a0", c.getPropertyName());

    c = mapping.get("b1");
    assertEquals("b", c.getDbColumn());
    assertEquals(1, c.getIndexPos());
    assertEquals("b1", c.getPropertyName());

    c = mapping.get("m2");
    assertEquals("MONTH(MAKEDATE(2015, 241))", c.getDbColumn());
    assertEquals(2, c.getIndexPos());
    assertEquals("m2", c.getPropertyName());

    c = mapping.get("d3");
    assertEquals("d", c.getDbColumn());
    assertEquals(3, c.getIndexPos());
    assertEquals("d3", c.getPropertyName());


    c = mapping.get("e4");
    assertEquals("e", c.getDbColumn());
    assertEquals(4, c.getIndexPos());
    assertEquals("e4", c.getPropertyName());

  }

  @Test
  public void test_withAsAlias() {

    ColumnMapping columnMapping = DRawSqlColumnsParser.parse("a as a0,'b'    b1, \"c(blah)\" as c2 ,   d as   d3  , e     as e4 ");
    Map<String, Column> mapping = columnMapping.mapping();

    assertEquals(5, mapping.size());

    Column c = mapping.get("a0");

    assertEquals("a", c.getDbColumn());
    assertEquals(0, c.getIndexPos());
    assertEquals("a0", c.getPropertyName());

    c = mapping.get("b1");
    assertEquals("'b'", c.getDbColumn());
    assertEquals(1, c.getIndexPos());
    assertEquals("b1", c.getPropertyName());

    c = mapping.get("c2");
    assertEquals("\"c(blah)\"", c.getDbColumn());
    assertEquals(2, c.getIndexPos());
    assertEquals("c2", c.getPropertyName());

    c = mapping.get("d3");
    assertEquals("d", c.getDbColumn());
    assertEquals(3, c.getIndexPos());
    assertEquals("d3", c.getPropertyName());


    c = mapping.get("e4");
    assertEquals("e", c.getDbColumn());
    assertEquals(4, c.getIndexPos());
    assertEquals("e4", c.getPropertyName());

  }

  @Test
  public void test_doubleColon() {

    ColumnMapping columnMapping = DRawSqlColumnsParser.parse("a,MD5(id::text) as b,c");
    Map<String, Column> mapping = columnMapping.mapping();
    Column c = mapping.get("a");

    assertEquals("a", c.getDbColumn());
    assertEquals(0, c.getIndexPos());
    assertEquals("a", c.getPropertyName());

    c = mapping.get("b");
    assertEquals("MD5(id::text)", c.getDbColumn());
    assertEquals(1, c.getIndexPos());
    assertEquals("b", c.getPropertyName());

    c = mapping.get("c");
    assertEquals("c", c.getDbColumn());
    assertEquals(2, c.getIndexPos());
    assertEquals("c", c.getPropertyName());

  }

}
