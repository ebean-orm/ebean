package org.tests.insert;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import org.tests.model.basic.EBasic;
import org.tests.model.basic.EBasic.Status;

public class MainDbCopy {

  public static void main(String[] args) {

    EbeanServer defaultServer = Ebean.getServer(null);

    EBasic e = new EBasic();
    e.setName("blah");
    e.setStatus(Status.NEW);
    e.setDescription(null);

    defaultServer.save(e);

    EBasic e1 = defaultServer.find(EBasic.class, e.getId());

    EbeanServer serverDest = Ebean.getServer("mysql");

    serverDest.insert(e1);


  }
}
