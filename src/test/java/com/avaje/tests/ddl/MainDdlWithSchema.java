package com.avaje.tests.ddl;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.tests.model.basic.EBasic;

public class MainDdlWithSchema {

  public static void main(String[] args) {

    ServerConfig cfg = new ServerConfig();
    cfg.setName("h2");
    cfg.loadFromProperties();
    cfg.addClass(EBasic.class);

    // .... other settings ...
    //UnderscoreNamingConvention naming = new UnderscoreNamingConvention();
    //naming.setSchema("test");
    //cfg.setNamingConvention(naming);
    cfg.setDdlGenerate(true);
    cfg.setDdlRun(true);
    EbeanServer ebean = EbeanServerFactory.create(cfg);
    //ebean.createSqlUpdate("create schema foo").execute();
    System.out.println(ebean);
  }
}
