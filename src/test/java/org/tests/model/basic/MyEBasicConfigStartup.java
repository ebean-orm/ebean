package org.tests.model.basic;

import io.ebean.config.ServerConfig;
import io.ebean.event.AbstractBeanPersistListener;
import io.ebean.event.BulkTableEvent;
import io.ebean.event.BulkTableEventListener;
import io.ebean.event.ServerConfigStartup;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class MyEBasicConfigStartup implements ServerConfigStartup {

  public static final AtomicLong insertCount = new AtomicLong();
  public static final AtomicLong updateCount = new AtomicLong();
  public static final AtomicLong deleteCount = new AtomicLong();

  public static void resetCounters() {
    insertCount.set(0);
    updateCount.set(0);
    deleteCount.set(0);
  }

  @Override
  public void onStart(ServerConfig serverConfig) {

    serverConfig.add(new EbasicPersistList());
    serverConfig.add(new EbasicBulkListener());
  }

  public static class EbasicBulkListener implements BulkTableEventListener {

    final Set<String> s = new HashSet<>();

    EbasicBulkListener() {
      s.add("e_basic");
    }

    @Override
    public Set<String> registeredTables() {
      return s;
    }

    @Override
    public void process(BulkTableEvent bulkTableEvent) {
      System.out.println("-- " + bulkTableEvent);
    }

  }

  public static class EbasicPersistList extends AbstractBeanPersistListener {

    @Override
    public boolean isRegisterFor(Class<?> cls) {
      return EBasic.class.isAssignableFrom(cls);
    }

    @Override
    public void inserted(Object bean) {
      insertCount.incrementAndGet();
      System.out.println("-- EBasic inserted " + ((EBasic) bean).getId());
    }

    @Override
    public void updated(Object bean, Set<String> updatedProperties) {
      updateCount.incrementAndGet();
      System.out.println("-- EBasic updated " + ((EBasic) bean).getId() + " updatedProperties: " + updatedProperties);
    }

    @Override
    public void deleted(Object bean) {
      deleteCount.incrementAndGet();
      System.out.println("-- EBasic deleted " + ((EBasic) bean).getId());
    }

  }

}
