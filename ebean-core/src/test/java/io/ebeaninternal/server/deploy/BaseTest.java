package io.ebeaninternal.server.deploy;

import io.ebean.DB;
import io.ebeaninternal.api.SpiEbeanServer;

public class BaseTest {

  protected SpiEbeanServer server = (SpiEbeanServer)DB.getDefault();

  protected <T> BeanDescriptor<T> getBeanDescriptor(Class<T> cls) {
    return server.descriptor(cls);
  }

}
