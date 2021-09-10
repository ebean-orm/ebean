package org.tests.callable;

import io.ebean.BaseTestCase;
import io.ebean.CallableSql;
import io.ebean.DB;
import io.ebean.Database;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasic;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestMysqlCallable extends BaseTestCase {

//  drop procedure my_stored_procedure if exists;
//
  /*

  DELIMITER //
  create procedure my_stored_procedure(in v_id integer, in v_name varchar(100))
  begin
  update e_basic set name = v_name where id = v_id;
  end;
  //

*/

  /**
   * Only run this test manually against MySQL with the above stored procedure.
   */
  @Disabled
  @Test
  public void test() {

    // mysql specific test
    Database server = DB.byName("mysql");

    EBasic basic = new EBasic();
    basic.setName("calling");
    server.save(basic);

    CallableSql cs = server.createCallableSql("{call my_stored_procedure(?,?)}");
    cs.setParameter(1, basic.getId());
    cs.setParameter(2, "modBySP");
    // without addModification() need to confirm transaction treated as not query only
    //cs.addModification("e_basic", false, true, false);
    server.execute(cs);


    EBasic basic1 = server.find(EBasic.class, basic.getId());

    assertEquals("modBySP", basic1.getName());
  }

}
