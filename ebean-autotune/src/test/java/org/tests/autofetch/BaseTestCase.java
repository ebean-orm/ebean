package org.tests.autofetch;

import io.ebean.DB;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;

public class BaseTestCase {

  protected SpiEbeanServer spiEbeanServer() {
    return (SpiEbeanServer) DB.getDefault();
  }

  protected <T> BeanDescriptor<T> getBeanDescriptor(Class<T> cls) {
    return spiEbeanServer().descriptor(cls);
  }
}
