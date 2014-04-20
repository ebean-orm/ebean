package com.avaje.tests.model.basic;

import java.util.HashSet;
import java.util.Set;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.event.BeanPersistListener;
import com.avaje.ebean.event.BulkTableEvent;
import com.avaje.ebean.event.BulkTableEventListener;
import com.avaje.ebean.event.ServerConfigStartup;

public class MyEBasicConfigStartup implements ServerConfigStartup {

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

  public static class EbasicPersistList implements BeanPersistListener<EBasic> {

    public boolean inserted(EBasic bean) {
      System.out.println("-- EBasic inserted " + bean.getId());
      return false;
    }

    public boolean updated(EBasic bean, Set<String> updatedProperties) {
      System.out.println("-- EBasic updated " + bean.getId()+" updatedProperties: "+updatedProperties);
      return false;
    }

    public boolean deleted(EBasic bean) {
      System.out.println("-- EBasic deleted " + bean.getId());
      return false;
    }

    public void remoteInsert(Object id) {
    }

    public void remoteUpdate(Object id) {
    }

    public void remoteDelete(Object id) {
    }

  }

}
