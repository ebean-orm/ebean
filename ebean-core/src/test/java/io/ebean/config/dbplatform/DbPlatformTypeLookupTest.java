package io.ebean.config.dbplatform;

import org.junit.Test;

import java.sql.Types;

import static org.junit.Assert.assertEquals;

public class DbPlatformTypeLookupTest {

  DbPlatformTypeLookup lookup = new DbPlatformTypeLookup();

  @Test
  public void byName() throws Exception {

    assertEquals(lookup.byName("DECIMAL"), DbType.DECIMAL);
    assertEquals(lookup.byName("Decimal"), DbType.DECIMAL);
    assertEquals(lookup.byName("decimal"), DbType.DECIMAL);

    assertEquals(lookup.byName("varchar"), DbType.VARCHAR);
    assertEquals(lookup.byName("varchar2"), DbType.VARCHAR);

    assertEquals(lookup.byName("float"), DbType.REAL);
    assertEquals(lookup.byName("real"), DbType.REAL);

    assertEquals(lookup.byName("uuid"), DbType.UUID);
    assertEquals(lookup.byName("hstore"), DbType.HSTORE);

    assertEquals(lookup.byName("json"), DbType.JSON);
    assertEquals(lookup.byName("jsonb"), DbType.JSONB);
    assertEquals(lookup.byName("jsonclob"), DbType.JSONCLOB);
    assertEquals(lookup.byName("jsonblob"), DbType.JSONBLOB);
    assertEquals(lookup.byName("jsonVarchar"), DbType.JSONVARCHAR);

  }

  @Test
  public void byId() throws Exception {

    assertEquals(lookup.byId(Types.ARRAY), DbType.ARRAY);
    assertEquals(lookup.byId(Types.BIGINT), DbType.BIGINT);

    assertEquals(lookup.byId(ExtraDbTypes.UUID), DbType.UUID);
    assertEquals(lookup.byId(ExtraDbTypes.HSTORE), DbType.HSTORE);

    assertEquals(lookup.byId(ExtraDbTypes.JSON), DbType.JSON);
    assertEquals(lookup.byId(ExtraDbTypes.JSONB), DbType.JSONB);
    assertEquals(lookup.byId(ExtraDbTypes.JSONClob), DbType.JSONCLOB);
    assertEquals(lookup.byId(ExtraDbTypes.JSONBlob), DbType.JSONBLOB);
    assertEquals(lookup.byId(ExtraDbTypes.JSONVarchar), DbType.JSONVARCHAR);
  }

}
