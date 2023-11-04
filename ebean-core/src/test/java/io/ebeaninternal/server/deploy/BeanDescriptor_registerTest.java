package io.ebeaninternal.server.deploy;

import io.ebean.Database;
import io.ebean.event.AbstractBeanPersistListener;
import io.ebean.event.BeanPersistAdapter;
import io.ebean.event.BeanPersistListener;
import io.ebeaninternal.api.SpiEbeanServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasic;

import static org.junit.jupiter.api.Assertions.*;

public class BeanDescriptor_registerTest {

  @Test
  public void testRegisterDeregister() {

    Database db = Database.builder()
      .setName("h2other")
      .loadFromProperties()
      .setDdlExtra(false)
      .setRegister(false)
      .setDefaultServer(false)
      .addClass(EBasic.class)
      .build();

    SpiEbeanServer ebeanServer = (SpiEbeanServer)db;
    try {
      BeanDescriptor<EBasic> desc = ebeanServer.descriptor(EBasic.class);
      persistListenerRegistrationTests(desc);
      persistControllerRegistrationTests(desc);
    } finally {
      ebeanServer.shutdown();
    }
  }

  private void persistControllerRegistrationTests(BeanDescriptor<EBasic> desc) {

    Controller1 controller1 = new Controller1();

    assertNull(desc.persistController());
    desc.register(controller1);
    assertSame(controller1, desc.persistController());

    Controller2 controller2 = new Controller2();
    desc.register(controller2);

    Assertions.assertEquals(2, ((ChainedBeanPersistController) desc.persistController()).size());

    desc.deregister(controller1);
    assertEquals(1, ((ChainedBeanPersistController) desc.persistController()).size());

    desc.deregister(controller2);
    assertEquals(0, ((ChainedBeanPersistController) desc.persistController()).size());
  }

  private void persistListenerRegistrationTests(BeanDescriptor<EBasic> desc) {

    Listener1 listener1 = new Listener1();

    assertNull(desc.persistListener());
    desc.register(listener1);
    assertSame(listener1, desc.persistListener());

    Listener2 listener2 = new Listener2();
    desc.register(listener2);

    BeanPersistListener persistListener = desc.persistListener();
    assertTrue(persistListener instanceof ChainedBeanPersistListener);
    assertEquals(2, ((ChainedBeanPersistListener) persistListener).size());

    desc.deregister(listener1);
    assertEquals(1, ((ChainedBeanPersistListener) desc.persistListener()).size());

    desc.deregister(listener2);
    assertEquals(0, ((ChainedBeanPersistListener) desc.persistListener()).size());
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
