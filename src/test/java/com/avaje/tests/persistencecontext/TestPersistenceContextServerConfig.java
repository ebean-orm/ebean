package com.avaje.tests.persistencecontext;

import com.avaje.ebean.*;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.tests.model.basic.EBasicVer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestPersistenceContextServerConfig extends BaseTestCase {

  @Test
  public void test_config() {

    SpiEbeanServer ebeanServer = (SpiEbeanServer)create();

    Query<EBasicVer> query = ebeanServer.find(EBasicVer.class);

    PersistenceContextScope scope = ebeanServer.getPersistenceContextScope((SpiQuery<?>) query);

    assertEquals(PersistenceContextScope.QUERY, scope);
  }

  static EbeanServer create() {

    ServerConfig config = new ServerConfig();
    config.setName("h2");
    config.loadFromProperties();
    config.setName("withPCQuery");
    config.setPersistenceContextScope(PersistenceContextScope.QUERY);

    config.addClass(EBasicVer.class);

    return EbeanServerFactory.create(config);
  }
}
