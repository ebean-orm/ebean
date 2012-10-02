package com.avaje.tests.basic;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.LogLevel;
import com.avaje.tests.model.embedded.EMain;

public class TestDynamicUpdate extends TestCase {

	public void testUpdate() {

		// insert
		EMain b = new EMain();
		b.setName("aaa");
		b.getEmbeddable().setDescription("123");

		EbeanServer server = Ebean.getServer(null);
		server.save(b);

		assertNotNull(b.getId());

		// reload object und update the name
		EMain b2 = server.find(EMain.class, b.getId());

		b2.getEmbeddable().setDescription("ABC");
		server.save(b2);

		//server.getAdminLogging().setLogLevel(LogLevel.SQL);
		
		server.beginTransaction().setLogLevel(LogLevel.SQL);
		try {
    		EMain b3 = server.find(EMain.class, b.getId());
    		assertEquals("ABC", b3.getEmbeddable().getDescription());
		} finally {
		    server.endTransaction();
		}
		EMain b4 = server.find(EMain.class, b.getId());
		b4.setName("bbb");
		b4.getEmbeddable().setDescription("123");
		server.save(b4);

		EMain b5 = server.find(EMain.class, b.getId());
		assertEquals("123", b5.getEmbeddable().getDescription());
	}
}
