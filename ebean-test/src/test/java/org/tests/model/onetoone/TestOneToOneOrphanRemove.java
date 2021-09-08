package org.tests.model.onetoone;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.annotation.IgnorePlatform;
import io.ebean.annotation.Platform;

import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOneToOneOrphanRemove extends BaseTestCase {

  @Test
  @IgnorePlatform(Platform.SQLSERVER)
  public void base() {

    OtoCust jack = new OtoCust("Jack");

    OtoCustAddress address = new OtoCustAddress("line1", "line2");

    jack.setAddress(address);
    DB.save(jack);

    // set new address
    OtoCustAddress address2 = new OtoCustAddress("other1", "other2");
    jack.setAddress(address2);

    // Fail do to uniqueness constraint
    LoggedSql.start();
    DB.save(jack);

    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(5);
    assertSql(sql.get(0)).contains("delete from oto_cust_address where aid=? and version=?");
    assertSqlBind(sql.get(1));
    assertSql(sql.get(2)).contains("update oto_cust set version=? where cid=? and version=?");
    assertThat(sql.get(3)).contains("insert into oto_cust_address ");
    assertThat(sql.get(4)).contains("-- bind(");

    jack.setAddress(null);
    DB.save(jack);

    sql = LoggedSql.stop();
    assertThat(sql).hasSize(3);
    assertSql(sql.get(0)).contains("delete from oto_cust_address where aid=? and version=?");
    assertSqlBind(sql.get(1));
    assertSql(sql.get(2)).contains("update oto_cust set version=? where cid=? and version=?");

    OtoCustAddress foundAddress = DB.find(OtoCustAddress.class, address2.getAid());
    assertThat(foundAddress).isNull();
  }

}
