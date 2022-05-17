package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.NamingConvention;
import io.ebean.config.dbplatform.IdType;
import io.ebeaninternal.dbmigration.migration.Column;
import io.ebeaninternal.dbmigration.migration.CreateTable;
import io.ebeaninternal.dbmigration.model.MTableIdentity;
import io.ebeaninternal.server.deploy.IdentityMode;

import java.util.ArrayList;
import java.util.List;

final class BaseTableIdentity {

  private final PlatformDdl platformDdl;
  private final NamingConvention namingConvention;
  private final CreateTable createTable;
  private final List<Column> pk = new ArrayList<>(3);

  BaseTableIdentity(CreateTable createTable, PlatformDdl platformDdl, NamingConvention namingConvention) {
    this.platformDdl = platformDdl;
    this.namingConvention = namingConvention;
    this.createTable = createTable;
    init(createTable.getColumn());
  }

  private void init(List<Column> columns) {
    for (Column column : columns) {
      if (Boolean.TRUE.equals(column.isPrimaryKey())) {
        pk.add(column);
      }
    }
  }

  DdlIdentity identity() {
    if (pk.size() != 1) {
      return DdlIdentity.NONE;
    }
    final IdentityMode identityMode = MTableIdentity.fromCreateTable(createTable);
    final IdType idType = platformDdl.useIdentityType(identityMode.getIdType());
    String sequenceName = identityMode.getSequenceName();
    if (IdType.SEQUENCE == idType && (sequenceName == null || sequenceName.isEmpty())) {
      sequenceName = deriveSequenceName();
    }
    return new DdlIdentity(idType, identityMode, sequenceName);
  }

  private String deriveSequenceName() {
    String columnName = pk.size() == 1 ? pk.get(0).getName() : "";
    return namingConvention.getSequenceName(createTable.getName(), columnName);
  }

  boolean hasPrimaryKey() {
    return !pk.isEmpty();
  }

  List<Column> pkColumns() {
    if (createTable.getPartitionMode() != null && platformDdl.addPartitionColumnToPrimaryKey()) {
      String partitionColumn = createTable.getPartitionColumn();
      if (!pkContains(partitionColumn)) {
        Column pc = new Column();
        pc.setName(partitionColumn);
        // future, could consider prefix option rather than suffix
        pk.add(pc);
      }
    }
    return pk;
  }

  private boolean pkContains(String partitionColumn) {
    for (Column column : pk) {
      if (column.getName().equals(partitionColumn)) {
        return true;
      }
    }
    return false;
  }
}
