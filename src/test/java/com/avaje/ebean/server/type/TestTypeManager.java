package com.avaje.ebean.server.type;

import java.sql.Types;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.H2Platform;
import com.avaje.ebeaninternal.server.core.BootupClasses;
import com.avaje.ebeaninternal.server.type.CtCompoundType;
import com.avaje.ebeaninternal.server.type.DefaultTypeManager;
import com.avaje.ebeaninternal.server.type.ScalarDataReader;
import com.avaje.ebeaninternal.server.type.ScalarType;
import com.avaje.ebeaninternal.server.type.reflect.CheckImmutableResponse;
import com.avaje.tests.model.ivo.CMoney;
import com.avaje.tests.model.ivo.ExhangeCMoneyRate;
import com.avaje.tests.model.ivo.Money;

public class TestTypeManager extends BaseTestCase {

  @Test
  public void test() {

    ServerConfig serverConfig = new ServerConfig();
    serverConfig.setDatabasePlatform(new H2Platform());

    BootupClasses bootupClasses = new BootupClasses();

    DefaultTypeManager typeManager = new DefaultTypeManager(serverConfig, bootupClasses);

    CheckImmutableResponse checkImmutable = typeManager.checkImmutable(Money.class);
    Assert.assertTrue(checkImmutable.isImmutable());

    checkImmutable = typeManager.checkImmutable(CMoney.class);
    Assert.assertTrue(checkImmutable.isImmutable());

    ScalarDataReader<?> dataReader = typeManager
        .recursiveCreateScalarDataReader(ExhangeCMoneyRate.class);
    Assert.assertTrue(dataReader instanceof CtCompoundType<?>);

    dataReader = typeManager.recursiveCreateScalarDataReader(CMoney.class);
    Assert.assertTrue(dataReader instanceof CtCompoundType<?>);

    ScalarType<?> scalarType = typeManager.recursiveCreateScalarTypes(Money.class);
    Assert.assertTrue(scalarType.getJdbcType() == Types.DECIMAL);
    Assert.assertTrue(!scalarType.isJdbcNative());
    Assert.assertEquals(Money.class, scalarType.getType());

  }

}
