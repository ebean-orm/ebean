package io.ebeaninternal.server.type;

import io.ebean.config.dbplatform.ExtraDbTypes;
import io.ebean.core.type.DocPropertyType;
import org.junit.Test;

import java.sql.SQLException;

public class ScalarTypeJsonListTest extends BasePlatformArrayTypeFactoryTest {

  @Test
  public void typeFor_expect_nullToEmpty_when_postgresNonNull() throws SQLException {

    assertBindNullTo_PGObjectEmpty(ScalarTypeJsonList.typeFor(true, ExtraDbTypes.JSONB, DocPropertyType.OBJECT, false));
    assertBindNullTo_PGObjectEmpty(ScalarTypeJsonList.typeFor(true, ExtraDbTypes.JSON, DocPropertyType.OBJECT, false));

    assertBindNullTo_EmptyString(ScalarTypeJsonList.typeFor(true, ExtraDbTypes.JSONVarchar, DocPropertyType.OBJECT, false));
  }

  @Test
  public void typeFor_expect_nullToNull_when_nullable() throws SQLException {

    assertBindNullTo_PGObjectNull(ScalarTypeJsonList.typeFor(true, ExtraDbTypes.JSONB, DocPropertyType.OBJECT, true));
    assertBindNullTo_PGObjectNull(ScalarTypeJsonList.typeFor(true, ExtraDbTypes.JSON, DocPropertyType.OBJECT, true));

    assertBindNullTo_Null(ScalarTypeJsonList.typeFor(true, ExtraDbTypes.JSONVarchar, DocPropertyType.OBJECT, true));
  }

}
