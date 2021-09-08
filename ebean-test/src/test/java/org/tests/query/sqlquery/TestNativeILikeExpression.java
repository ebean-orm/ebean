package org.tests.query.sqlquery;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.annotation.Platform;
import io.ebean.plugin.SpiServer;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNativeILikeExpression extends BaseTestCase {

  @Test
  public void test() {

    boolean expectNative = isExpectNative();

    ResetBasicData.reset();

    Query<Customer> query = DB.find(Customer.class)
      .where().ilike("name", "rob")
      .query();

    List<Customer> list = query.findList();

    if (expectNative) {
      assertSql(query).contains(" from o_customer t0 where t0.name ilike ?");
      assertThat(list).isNotEmpty();
    }
  }

  private boolean isExpectNative() {

    SpiServer pluginApi = server().pluginApi();
    boolean expressionNativeIlike = pluginApi.config().isExpressionNativeIlike();
    Platform platform = pluginApi.platform().base();

    return expressionNativeIlike && platform == Platform.POSTGRES;
  }
}
