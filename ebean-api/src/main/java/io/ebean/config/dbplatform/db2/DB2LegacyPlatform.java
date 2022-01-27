package io.ebean.config.dbplatform.db2;

import io.ebean.annotation.Platform;

/**
 * DB2 specific platform for older DB2 versions. This platform is here for
 * compatibility reasons. It uses a length limit of 18 chars for table and
 * constraint names. Newer DB2 versions will support up to 128. It is strongly
 * recommended to migrate to db2luw/DB2ForI or db2zos platform.
 */
public class DB2LegacyPlatform extends BaseDB2Platform {
  public DB2LegacyPlatform() {
    super();
    this.platform = Platform.DB2;
    // Note: DB2 (at least LUW supports length up to 128)
    // TOOD: Check if we need to introduce a new platform (DB2_LUW_11 ?)
    this.maxTableNameLength = 18;
    this.maxConstraintNameLength = 18;
  }
}
