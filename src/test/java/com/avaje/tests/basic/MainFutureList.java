package com.avaje.tests.basic;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Future;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.FutureList;
import com.avaje.ebean.Query;
import com.avaje.ebean.SqlFutureList;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;
import com.avaje.tests.model.basic.TOne;

public class MainFutureList {

	public static void main(String[] args) throws Exception {
	
		checkFutureRowCount(true);
		//testSqlQueryFuture();
		//testOrmFuture();
	}
	
	public void executeDDL(EbeanServer server, String ddl) {
	  
	  Transaction t  = server.createTransaction();
	  try {
	    Connection connection = t.getConnection()
	        ;
	    
	    
	  } finally {
	    t.end();
	  }
	  
	}
	
	private void executeStmt(Connection c, String ddl) throws SQLException {
	  java.sql.PreparedStatement pstmt = null;
	  try {
	    pstmt = c.prepareStatement(ddl);
	    pstmt.execute();
	    
	  } finally {
	    if (pstmt != null) {
	      pstmt.close();
	    }
	  }
	}
	
	private static EbeanServer createEbeanServer(boolean primary) {

		if (primary){
			return Ebean.getServer(null);
		}
		
		ServerConfig c = new ServerConfig();
		c.setName("pgtest");

//		// requires postgres driver in class path
//		DataSourceConfig postgresDb = new DataSourceConfig();
//		postgresDb.setDriver("org.postgresql.Driver");
//		postgresDb.setUsername("test");
//		postgresDb.setPassword("test");
//		postgresDb.setUrl("jdbc:postgresql://127.0.0.1:5432/test");
//		postgresDb.setHeartbeatSql("select count(*) from t_one");

		// requires oracle driver in class path
		DataSourceConfig oraDb = new DataSourceConfig();
		oraDb.setDriver("oracle.jdbc.driver.OracleDriver");
		oraDb.setUsername("junk");
		oraDb.setPassword("junk");
		oraDb.setUrl("jdbc:oracle:thin:junk/junk@localhost:1521:XE");
		oraDb.setHeartbeatSql("select count(*) from dual");
		
		
		c.loadFromProperties();
		c.setDdlGenerate(true);
		c.setDdlRun(true);
		c.setDefaultServer(false);
		c.setRegister(false);
//		c.setDataSourceConfig(postgresDb);
		c.setDataSourceConfig(oraDb);
		
		//c.setDatabaseBooleanTrue("1");
		//c.setDatabaseBooleanFalse("0");
		//c.setDatabaseBooleanTrue("T");
		//c.setDatabaseBooleanFalse("F");

		//c.setDatabasePlatform(new Postgres83Platform());

		c.addClass(TOne.class);

		return EbeanServerFactory.create(c);

	}

	public static void checkFutureRowCount(boolean primay) throws Exception {
		
		ResetBasicData.reset();
		
		EbeanServer server = createEbeanServer(primay);
		
		Query<Order> query = server.find(Order.class);
		Future<Integer> futureRowCount = server.findFutureRowCount(query, null);
		boolean done = futureRowCount.isDone();
		
		System.out.println("done: "+done);
		
		Integer rowCount = futureRowCount.get();
		System.out.println("got rc:"+rowCount);
		
		
	}
	
	public static void checkSqlQueryFuture(boolean primay) throws Exception {
	
		EbeanServer server = createEbeanServer(primay);
		
		String sql = "select o.* from all_tables o";
		SqlQuery sqlQuery = server.createSqlQuery(sql);
		
		SqlFutureList list = server.findFutureList(sqlQuery, null);
		System.out.println("start done:"+list.isDone());
		Thread.sleep(200);
		if (!list.isDone()){
			list.cancel(true);
		}
		
		if (!list.isCancelled()){
			List<SqlRow> list2 = list.get();
			System.out.println("got "+list2.size());
		}
		
		Thread.sleep(3000);
		System.out.println("done sleeping");
	}
	
	public static void checkOrmFuture() throws Exception {
			
			
		ResetBasicData.reset();
		
		//EbeanServer server = Ebean.getServer(null);
		
		Query<Order> query = Ebean.find(Order.class);
		
		FutureList<Order> futureList = query.findFutureList();
		
		Thread.sleep(3000);
		System.out.println("end of sleep");
				
		if (!futureList.isDone()){
			futureList.cancel(true);
		}
		
		System.out.println("and... done:"+futureList.isDone());
		
		if (!futureList.isCancelled()){
			//List<Order> l0 = futureList.get(30, TimeUnit.SECONDS);
			List<Order> list = futureList.get();
			System.out.println("list:"+list);
		}
		
		System.out.println("done");
		
	}
}
