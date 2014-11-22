package com.avaje.ebeaninternal.server.util;

import com.avaje.ebeaninternal.api.BindParams;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class BindParamsParserTest {

  @Test
  public void testParse() throws Exception {

    String dml = "delete from foo where id in (:ids)";
    BindParams bindParams = new BindParams();

    bindParams.setParameter("ids", Arrays.asList("1", "2", "3"));
    String sql1 = BindParamsParser.parse(bindParams, dml);
    assertEquals("delete from foo where id in (?,?,?)", sql1);

    bindParams.setParameter("ids", Arrays.asList("451", "52"));
    sql1 = BindParamsParser.parse(bindParams, dml);
    assertEquals("delete from foo where id in (?,?)", sql1);

    bindParams.setParameter("ids", Arrays.asList("545", "656"));
    sql1 = BindParamsParser.parse(bindParams, dml);
    assertEquals("delete from foo where id in (?,?)", sql1);

    bindParams.setParameter("ids", Arrays.asList("545df", "df656", "SDF", "sdf"));
    sql1 = BindParamsParser.parse(bindParams, dml);
    assertEquals("delete from foo where id in (?,?,?,?)", sql1);

  }
}