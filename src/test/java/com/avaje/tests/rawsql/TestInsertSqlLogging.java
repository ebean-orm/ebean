package com.avaje.tests.rawsql;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlUpdate;

public class TestInsertSqlLogging extends TestCase {

    public void test() {
        
        //Ebean.getServer(null);
        
        String sql = "insert into audit_log (id, description, modified_description) values (?,?,?)";
        SqlUpdate sqlUpdate = Ebean.createSqlUpdate(sql);
        sqlUpdate.setParameter(1, 10000);
        sqlUpdate.setParameter(2, "hello");
        sqlUpdate.setParameter(3, "rob");
        
        sqlUpdate.execute();
        
  
    }
}
