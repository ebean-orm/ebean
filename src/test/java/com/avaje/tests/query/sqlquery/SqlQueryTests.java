package com.avaje.tests.query.sqlquery;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlQueryListener;
import com.avaje.ebean.SqlRow;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

public class SqlQueryTests {

  @Test
  public void setListener() {


    ResetBasicData.reset();

    SqlQuery sqlQuery = Ebean.createSqlQuery("select * from o_order");
    sqlQuery.setListener(new SqlQueryListener() {
      @Override
      public void process(SqlRow bean) {
        System.out.println("process row "+bean);
      }
    });
    // returns an empty list
    sqlQuery.findList();

  }
}
