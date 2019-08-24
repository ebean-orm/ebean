package io.ebeaninternal.server.rawsql;

import org.junit.Test;

import static org.junit.Assert.*;

public class DRawSqlServiceTest {

  private final DRawSqlService dRawSqlService = new DRawSqlService();

  @Test
  public void combine() {

    assertEquals("mycol", dRawSqlService.combine(null, null, "mycol"));
    assertEquals("mytable.mycol", dRawSqlService.combine(null, "mytable", "mycol"));
    assertEquals("myschema.mytable.mycol", dRawSqlService.combine("myschema", "mytable", "mycol"));
    assertEquals("myschema.mycol", dRawSqlService.combine("myschema", null, "mycol"));
  }
}
