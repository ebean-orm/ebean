package com.avaje.tests.lib;

import junit.framework.TestCase;
import junit.framework.TestResult;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Transaction;

/**
 * The base class for all Ebean test to get access to the Ebean server and do
 * some cleanup stuff after a test has run
 */
public abstract class EbeanTestCase extends TestCase {
	
	@Override
	public void run(TestResult testResult) {
		try {
			super.run(testResult);
		} finally {
			Transaction tx = getServer().currentTransaction();
			if (tx != null && tx.isActive()) {
				// transaction left running after the test, rollback it to make
				// the environment ready for the next test
				tx.rollback();
			}
		}
	}

	public EbeanServer getServer() {
		
		return Ebean.getServer(null);
	}
}
