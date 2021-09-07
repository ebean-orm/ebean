package io.ebeaninternal.server.changelog;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.event.changelog.ChangeSet;
import org.junit.jupiter.api.Test;

public class DefaultChangeLogListenerTest extends BaseTestCase {

  Helper helper = new Helper();

  @Test
  public void test() {

    DefaultChangeLogListener changeLogListener = new DefaultChangeLogListener();

    Database defaultServer = DB.getDefault();
    changeLogListener.configure(defaultServer.pluginApi());

    ChangeSet changeSet = helper.createChangeSet("INT-001", 13);

    changeLogListener.log(changeSet);
  }

}
