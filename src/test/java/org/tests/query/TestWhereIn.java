package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.junit.Test;
import org.tests.model.basic.Country;
import org.tests.model.basic.ResetBasicData;

import static org.assertj.core.api.Assertions.assertThat;

public class TestWhereIn extends BaseTestCase {

  @Test
  public void testInVarchar() {

    ResetBasicData.reset();

    Query<Country> query = Ebean.find(Country.class)
      .where().in("code", "NZ", "AU")
      .query();

    query.findList();
    if (isPostgres()) {
      assertThat(sqlOf(query)).contains(" = any(");
    }

  }


  @Test
  public void testNotInVarchar() {

    ResetBasicData.reset();

    Query<Country> query = Ebean.find(Country.class)
      .where().notIn("code", "NZ", "SA", "US")
      .query();

    query.findList();
    if (isPostgres()) {
      assertThat(sqlOf(query)).contains(" != all(");
    }
  }
}
