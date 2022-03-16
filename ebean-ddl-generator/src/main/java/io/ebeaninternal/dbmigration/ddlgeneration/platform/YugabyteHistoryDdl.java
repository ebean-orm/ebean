package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.model.MTable;

public class YugabyteHistoryDdl extends PostgresHistoryDdl {

  @Override
  protected void createHistoryTable(DdlBuffer apply, MTable table) {
    createHistoryTableAs(apply, table);
    writeColumnDefinition(apply, sysPeriod, "tstzrange");
    apply.newLine().append(")").endOfStatement();
  }
}
