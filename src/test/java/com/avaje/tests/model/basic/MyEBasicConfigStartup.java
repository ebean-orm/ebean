package com.avaje.tests.model.basic;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.event.*;

public class MyEBasicConfigStartup implements ServerConfigStartup {

  public static AtomicLong insertCount = new AtomicLong();
  public static AtomicLong updateCount = new AtomicLong();
  public static AtomicLong deleteCount = new AtomicLong();

  public static void resetCounters() {
    insertCount.set(0);
    updateCount.set(0);
    deleteCount.set(0);
  }

  public void onStart(ServerConfig serverConfig) {

    serverConfig.add(new EbasicPersistList());
    serverConfig.add(new EbasicBulkListener());
  }

  public static class EbasicBulkListener implements BulkTableEventListener {

    final Set<String> s = new HashSet<String>();

    EbasicBulkListener() {
      s.add("e_basic");
    }

    public Set<String> registeredTables() {
      return s;
    }

    public void process(BulkTableEvent bulkTableEvent) {
      System.out.println("-- " + bulkTableEvent);
    }

  }

  public static class EbasicPersistList extends AbstractBeanPersistListener {

    @Override
    public boolean isRegisterFor(Class<?> cls) {
      return EBasic.class.isAssignableFrom(cls);
    }

    public boolean inserted(Object bean) {
      insertCount.incrementAndGet();
      System.out.println("-- EBasic inserted " + ((EBasic)bean).getId());
      return false;
    }

    public boolean updated(Object bean, Set<String> updatedProperties) {
      updateCount.incrementAndGet();
      System.out.println("-- EBasic updated " + ((EBasic)bean).getId()+" updatedProperties: "+updatedProperties);
      return false;
    }

    public boolean deleted(Object bean) {
      deleteCount.incrementAndGet();
      System.out.println("-- EBasic deleted " + ((EBasic)bean).getId());
      return false;
    }

  }

}
