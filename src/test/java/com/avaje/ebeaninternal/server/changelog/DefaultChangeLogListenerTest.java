package com.avaje.ebeaninternal.server.changelog;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.event.changelog.ChangeSet;
import org.junit.Test;

public class DefaultChangeLogListenerTest extends BaseTestCase {

  Helper helper = new Helper();

  @Test
  public void test() {

    DefaultChangeLogListener changeLogListener = new DefaultChangeLogListener();

    EbeanServer defaultServer = Ebean.getDefaultServer();
    changeLogListener.configure(defaultServer.getPluginApi());

    ChangeSet changeSet = helper.createChangeSet("INT-001", 13);

    changeLogListener.log(changeSet);
  }

}