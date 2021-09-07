package org.tests.insert;

import io.ebean.DB;
import io.ebean.Database;
import org.tests.model.basic.EBasic;
import org.tests.model.basic.EBasic.Status;

public class MainDbCopy {

  public static void main(String[] args) {

    Database defaultServer = DB.getDefault();

    EBasic e = new EBasic();
    e.setName("blah");
    e.setStatus(Status.NEW);
    e.setDescription(null);

    defaultServer.save(e);

    EBasic e1 = defaultServer.find(EBasic.class, e.getId());

    Database serverDest = DB.byName("mysql");
    serverDest.insert(e1);
  }
}
