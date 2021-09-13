package org.tests.model.basic.test;

import io.ebean.DB;
import io.ebean.annotation.Platform;
import io.ebeaninternal.api.SpiEbeanServer;

public class BaseTestCase {

  protected boolean isSqlServer() {
    return Platform.SQLSERVER == platform();
  }

  protected Platform platform() {
    return spiEbeanServer().platform().base();
  }

  protected SpiEbeanServer spiEbeanServer() {
    return (SpiEbeanServer) DB.getDefault();
  }

}
