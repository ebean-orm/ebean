package org.tests.json;

import io.ebean.DB;
import io.ebean.annotation.Platform;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.json.EBasicJsonBString;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestDbJsonBStringType {

  @Test
  void test() {
    var bean = new EBasicJsonBString("hi").content("{\"mykey\": 42}"); // JsonB format with space
    DB.save(bean);

    var found = DB.find(EBasicJsonBString.class, bean.id());
    // Note that Postgres JsonB will format the result
    assertThat(found.content()).isEqualTo("{\"mykey\": 42}");

    LoggedSql.start();

    // change title only, expect content not in update
    found.title("changeTitleOnly");
    DB.save(found);
    List<String> sql = LoggedSql.collect();
    // update does NOT contain our json content
    assertThat(sql.get(0)).contains("update ebasic_json_bstring set title=?, version=? where id=? and version=?");

    // change title and content
    found.title("changeAgain");
    found.content("{\"mykey\": 92}");
    DB.save(found);
    sql = LoggedSql.collect();
    assertThat(sql.get(0)).contains("update ebasic_json_bstring set title=?, content=?, version=? where id=? and version=?");


    // change content only
    found.content("{\"mykey\": 95, \"other\": \"AI\"}");
    DB.save(found);
    sql = LoggedSql.stop();
    assertThat(sql.get(0)).contains("update ebasic_json_bstring set content=?, version=? where id=? and version=?");


    if (DB.getDefault().platform() == Platform.POSTGRES) {
      List<EBasicJsonBString> result = DB.find(EBasicJsonBString.class)
        .where()
        .raw("content -> 'other' ?? ?", "AI")
        .raw("jsonb_exists(content -> 'other', ?)", "AI")
        .raw("jsonb_path_exists(content, '$.other ? (@ >= $param)', '{\"param\":\"AI\"}')")
        .findList();

      assertThat(result).hasSize(1);
    }
  }
}
