package io.ebean.platform.db2;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.DbType;
import io.ebean.config.dbplatform.SqlErrorCodes;

import java.sql.Types;

/**
 * DB2 specific platform for i Series.
 *
 * @author Cédric Sougné
 */
public class DB2ForIPlatform extends BaseDB2Platform {

  public DB2ForIPlatform() {
    super();
    this.platform = Platform.DB2FORI;
    // Note: IBM i from 7.1 allow up to to 128
    // TODO: Check if we need to introduce older platform (DB2ForI_6 ? but older
    // documentation is not anymore published on ibm.com),
    this.maxTableNameLength = 128;
    this.maxConstraintNameLength = 128;

    this.dbIdentity.setSupportsIdentity(true);

    this.exceptionTranslator = new SqlErrorCodes().addAcquireLock("57033") // key -913
        .addDuplicateKey("23505") // -803
        // .addDataIntegrity("-407","-530","-531","-532","-543","-544","-545","-603","-667")
        // we need SQLState, not code:
        // https://www.ibm.com/support/knowledgecenter/en/SSEPEK_10.0.0/codes/src/tpc/db2z_n.html
        .addDataIntegrity("23502", "23503", "23504", "23507", "23511", "23512", "23513", "42917", "23515")
        .build();

    booleanDbType = Types.SMALLINT;
    dbTypeMap.put(DbType.BOOLEAN, new DbPlatformType("smallint default 0"));
  }
}
