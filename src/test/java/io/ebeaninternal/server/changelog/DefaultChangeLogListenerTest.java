package io.ebeaninternal.server.changelog;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.event.changelog.ChangeSet;
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
