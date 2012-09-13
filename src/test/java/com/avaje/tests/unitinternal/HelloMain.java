

package com.avaje.tests.unitinternal;

import java.io.File;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.tests.model.basic.TOne;

public class HelloMain {

	public static void main(String[] args) {
	       // ### Configuration Objects ###
	       ServerConfig serverConfig = new ServerConfig();
	       DataSourceConfig dataSourceConfig = new DataSourceConfig();

	       // ### Configuration Settings ###
	       // -> data source
	       dataSourceConfig.setDriver("org.h2.Driver");
	       dataSourceConfig.setUsername("howtouser");
	       dataSourceConfig.setPassword("");
	       dataSourceConfig.setUrl("jdbc:h2:db/howto1");

	       // -> server
	       serverConfig.setName("default");
	       serverConfig.setDataSourceConfig(dataSourceConfig);

	       //  auto create db if it does not exist
	       if(!(new File("db/howto1.data.db")).exists()  ){
	           serverConfig.setDdlGenerate(true);
	           serverConfig.setDdlRun(true);
	           serverConfig.addClass(TOne.class);
	       }

	       EbeanServer eServer = EbeanServerFactory.create(serverConfig);

	       long id = 1;
	       TOne data = eServer.find(TOne.class, id);
	       if (data == null) {
	           System.out.println("This is the first run, saving data..");
	           TOne tone = new TOne();
	           tone.setName("banan");
	           eServer.save(tone);//new TOne()id, "Hello World!"));
	       } else {
	           System.out.println(String.format("############\n%s############", data.getName()));
	       }
	       //ShutdownManager.shutdown();
	   }
	
}
