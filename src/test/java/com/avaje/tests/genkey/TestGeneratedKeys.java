package com.avaje.tests.genkey;

import org.junit.Assert;
import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.TOne;

public class TestGeneratedKeys extends TestCase {

	
	public void testJdbcBatchInsert() {
		
		TOne c = new TOne();
		c.setName("Banana");
		c.setDescription("Test Gen Key");
		
		TOne c1 = new TOne();
		c1.setName("Two");
		c1.setDescription("Test Gen Key Two");
		
		Ebean.beginTransaction();
		try {
			Ebean.save(c);
			Ebean.save(c1);
		} finally {
			Ebean.commitTransaction();
		}
		Integer id = c.getId();
		Assert.assertNotNull("Get Id back after insert", id);

		Integer id1 = c1.getId();
		Assert.assertNotNull("Get Id back after insert", id1);

	}
	
//	public void testRaw() {
//
//		Ebean.createUpdate(TOne.class, "delete from tone").execute();
//
//		Transaction txn = Ebean.beginTransaction();
//		
//		// first delete all the rows in this table...
//		try {
//			Connection conn = txn.getConnection();
//			
////			String sql = "insert into t_oneb (name, description, active) values (?, ?, ?)";
////			PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
//
////			String[] genKeys = new String[]{"ID"};
////			String sql = "insert into t_oneb (id, name, description, active) values (t_oneb_seq.nextval, ?, ?, ?)";
////			PreparedStatement pstmt = conn.prepareStatement(sql, genKeys);
//
//			String sql = "insert into t_oneb (id, name, description, active) values (?, ?, ?, ?)";
//			PreparedStatement pstmt = conn.prepareStatement(sql);
//
//			ExtendedPreparedStatement p = (ExtendedPreparedStatement)pstmt;
//			PreparedStatement  p2 = p.getDelegate();
//			OraclePreparedStatement p3 = ((OraclePreparedStatement)p2);
//			p3.setExecuteBatch (3);
//
////			int i = 0;
//
//			int i = 1;
//			pstmt.setInt(1, 1);
//			pstmt.setString(i+1, "name");
//			pstmt.setString(i+2, "desc");
//			pstmt.setBoolean(i+3, true);
//			int r0 = pstmt.executeUpdate();
//			
////			ResultSet rset = pstmt.getGeneratedKeys();
////			while(rset.next()){
////				Object id = rset.getObject(1);
////				System.out.println("ID "+id);
////			}
////			rset.close();
//			
//			//pstmt.addBatch();
//			
//			pstmt.setInt(1, 2);
//			pstmt.setString(i+1, "name");
//			pstmt.setString(i+2, "desc2");
//			pstmt.setBoolean(i+3, true);
//			int r1 = pstmt.executeUpdate();
//			
//			int r2 = p3.sendBatch();
//			
//			System.out.println(r0+" "+r1+" "+r2);
//			
////			pstmt.addBatch();
////			
////			int[] rc = pstmt.executeBatch();
////			if (rc.length != 2){
////				System.out.println("Length "+rc.length);
////			}
//			
//			pstmt.close();
//			
////			ResultSet rset = pstmt.getGeneratedKeys();
////			while(rset.next()){
////				Object id = rset.getObject(1);
////				System.out.println("ID "+id);
////			}
//			
//		} catch (SQLException e){
//			throw new RuntimeException(e);
//			
//		} finally {
//			txn.commit();
//		}	
//	}

}
