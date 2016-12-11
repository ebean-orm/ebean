package io.ebeaninternal.server.deploy;

import io.ebean.EbeanServerFactory;
import io.ebean.config.ServerConfig;
import io.ebean.event.AbstractBeanPersistListener;
import io.ebean.event.BeanPersistAdapter;
import io.ebean.event.BeanPersistListener;
import io.ebeaninternal.api.SpiEbeanServer;
import org.tests.model.basic.EBasic;
import org.junit.Test;

import static org.junit.Assert.*;

public class BeanDescriptor_registerTest {

  @Test
  public void testRegisterDeregister() throws Exception {

    System.setProperty("ebean.ignoreExtraDdl", "true");

    ServerConfig config = new ServerConfig();

    config.setName("h2other");
    config.loadFromProperties();
    config.setRegister(false);
    config.setDefaultServer(false);
    config.getClasses().add(EBasic.class);

    SpiEbeanServer ebeanServer = (SpiEbeanServer) EbeanServerFactory.create(config);
    BeanDescriptor<EBasic> desc = ebeanServer.getBeanDescriptor(EBasic.class);

    persistListenerRegistrationTests(desc);
    persistControllerRegistrationTests(desc);
  }

  private void persistControllerRegistrationTests(BeanDescriptor<EBasic> desc) {

    Controller1 controller1 = new Controller1();

    assertNull(desc.getPersistController());
    desc.register(controller1);
    assertSame(controller1, desc.getPersistController());

    Controller2 controller2 = new Controller2();
    desc.register(controller2);

    assertEquals(2, ((ChainedBeanPersistController) desc.getPersistController()).size());

    desc.deregister(controller1);
    assertEquals(1, ((ChainedBeanPersistController) desc.getPersistController()).size());

    desc.deregister(controller2);
    assertEquals(0, ((ChainedBeanPersistController) desc.getPersistController()).size());
  }

  private void persistListenerRegistrationTests(BeanDescriptor<EBasic> desc) {

    Listener1 listener1 = new Listener1();

    assertNull(desc.getPersistListener());
    desc.register(listener1);
    assertSame(listener1, desc.getPersistListener());

    Listener2 listener2 = new Listener2();
    desc.register(listener2);

    BeanPersistListener persistListener = desc.getPersistListener();
    assertTrue(persistListener instanceof ChainedBeanPersistListener);
    assertEquals(2, ((ChainedBeanPersistListener) persistListener).size());

    desc.deregister(listener1);
    assertEquals(1, ((ChainedBeanPersistListener) desc.getPersistListener()).size());

    desc.deregister(listener2);
    assertEquals(0, ((ChainedBeanPersistListener) desc.getPersistListener()).size());
  }

  public static class Listener1 extends AbstractBeanPersistListener {

    @Override
    public boolean isRegisterFor(Class<?> cls) {
      return EBasic.class.isAssignableFrom(cls);
    }

  }

  public static class Listener2 extends AbstractBeanPersistListener {

    @Override
    public boolean isRegisterFor(Class<?> cls) {
      return EBasic.class.isAssignableFrom(cls);
    }

  }

  public static class Controller1 extends BeanPersistAdapter {

    @Override
    public boolean isRegisterFor(Class<?> cls) {
      return EBasic.class.isAssignableFrom(cls);
    }
  }

  public static class Controller2 extends BeanPersistAdapter {

    @Override
    public boolean isRegisterFor(Class<?> cls) {
      return EBasic.class.isAssignableFrom(cls);
    }
  }
}
