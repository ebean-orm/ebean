package com.avaje.tests.query;

import java.util.List;

import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.MyAdHoc;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestMyAdHocSqlSelect extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    String sql = "select order_id, count(*) as detailCount from o_order_detail group by order_id";
    RawSql rawSql = RawSqlBuilder.parse(sql).columnMapping("order_id", "order.id").create();

    List<MyAdHoc> list = Ebean.find(MyAdHoc.class).setRawSql(rawSql).where().gt("order_id", 0).having()
        .gt("detailCount", 0).findList();

    Assert.assertNotNull(list);
  }

}
