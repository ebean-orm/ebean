package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.model.MTable;

import java.io.IOException;

public class YugabyteHistoryDdl extends PostgresHistoryDdl {

  @Override
  protected void createHistoryTable(DdlBuffer apply, MTable table) throws IOException {
    createHistoryTableAs(apply, table);
    writeColumnDefinition(apply, sysPeriod, "tstzrange");
    apply.newLine().append(")").endOfStatement();
  }
}
