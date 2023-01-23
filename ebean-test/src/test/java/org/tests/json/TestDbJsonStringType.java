package org.tests.json;

import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.json.EBasicJsonString;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestDbJsonStringType {

  @Test
  void test() {
    var bean = new EBasicJsonString("hi").content("{\"mykey\": 52}"); // JsonB format with space
    DB.save(bean);

    var found = DB.find(EBasicJsonString.class, bean.id());
    // Note that Postgres JsonB will format the result
    assertThat(found.content()).isEqualTo("{\"mykey\": 52}");

    LoggedSql.start();

    // change title only, expect content not in update
    found.title("changeTitleOnly");
    DB.save(found);
    List<String> sql = LoggedSql.collect();
    // update does NOT contain our json content
    assertThat(sql.get(0)).contains("update ebasic_json_string set title=?, version=? where id=? and version=?");

    // change title and content
    found.title("changeAgain");
    found.content("{\"mykey\": 92}");
    DB.save(found);
    sql = LoggedSql.collect();
    assertThat(sql.get(0)).contains("update ebasic_json_string set title=?, content=?, version=? where id=? and version=?");


    // change content only
    found.content("{\"mykey\": 95}");
    DB.save(found);
    sql = LoggedSql.stop();
    assertThat(sql.get(0)).contains("update ebasic_json_string set content=?, version=? where id=? and version=?");

  }
}
