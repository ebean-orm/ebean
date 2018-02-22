package org.tests.model.onetoone;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOneToOneOrphanRemove extends BaseTestCase {

  @Test
  public void base() {

    OtoCust jack = new OtoCust("Jack");

    OtoCustAddress address = new OtoCustAddress("line1", "line2");

    jack.setAddress(address);
    Ebean.save(jack);

    // set new address
    OtoCustAddress address2 = new OtoCustAddress("other1", "other2");
    jack.setAddress(address2);

    // Fail do to uniqueness constraint
    LoggedSqlCollector.start();
    Ebean.save(jack);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(3);
    assertThat(sql.get(0)).contains("delete from oto_cust_address where aid=? and version=?");
    assertThat(sql.get(1)).contains("update oto_cust set version=? where cid=? and version=?");
    assertThat(sql.get(2)).contains("insert into oto_cust_address ");

    jack.setAddress(null);
    Ebean.save(jack);

    sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("delete from oto_cust_address where aid=? and version=?");
    assertThat(sql.get(1)).contains("update oto_cust set version=? where cid=? and version=?");

    OtoCustAddress foundAddress = Ebean.find(OtoCustAddress.class, address2.getAid());
    assertThat(foundAddress).isNull();
  }

}
