package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.platform.hana.HanaPlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.migration.Column;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HanaDdlTest {

  @Test
  public void alterTableDropColumn() {
    HanaColumnStoreDdl ddl = new HanaColumnStoreDdl(new HanaPlatform());
    DdlWrite writer = new DdlWrite();
    ddl.alterTableDropColumn(writer, "my_table", "my_column");
    assertEquals("-- apply alter tables\nCALL usp_ebean_drop_column('my_table', 'my_column');\n", writer.toString());
  }

  @Test
  public void alterTableAddColumn() {
    HanaColumnStoreDdl ddl = new HanaColumnStoreDdl(new HanaPlatform());
    DdlWrite writer = new DdlWrite();
    Column column = new Column();
    column.setName("my_column");
    column.setComment("comment");
    column.setDefaultValue("1");
    column.setNotnull(Boolean.TRUE);
    column.setType("int");
    column.setUnique("unique");
    column.setPrimaryKey(Boolean.TRUE);
    column.setCheckConstraint("CHECK(my_column > 0)");
    column.setCheckConstraintName("check_constraint");
    column.setHistoryExclude(Boolean.TRUE);
    column.setIdentity(Boolean.TRUE);
    ddl.alterTableAddColumn(writer, "my_table", column, false, "1");
    assertEquals(
      "-- apply alter tables\nalter table my_table add (my_column int default 1 not null);\n-- apply post alter\nalter table my_table add constraint check_constraint CHECK(my_column > 0);\n",
      writer.toString());
  }
}
