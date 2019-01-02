package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.dbplatform.hana.HanaPlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.migration.Column;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class HanaDdlTest {

  @Test
  public void alterTableDropColumn() throws IOException {
    HanaColumnStoreDdl ddl = new HanaColumnStoreDdl(new HanaPlatform());
    DdlWrite write = new DdlWrite();
    ddl.alterTableDropColumn(write.apply(), "my_table", "my_column");
    assertEquals("CALL usp_ebean_drop_column('my_table', 'my_column');\n", write.apply().getBuffer());
  }

  @Test
  public void alterTableAddColumn() throws IOException {
    HanaColumnStoreDdl ddl = new HanaColumnStoreDdl(new HanaPlatform());
    DdlWrite write = new DdlWrite();
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
    ddl.alterTableAddColumn(write.apply(), "my_table", column, false, "1");
    assertEquals("alter table my_table add ( my_column int default 1 not null);\nalter table my_table add constraint check_constraint CHECK(my_column > 0);\n", write.apply().getBuffer());
  }
}
