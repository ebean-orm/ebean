package org.tests.query.sqlquery;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.annotation.Platform;
import io.ebean.plugin.SpiServer;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNativeILikeExpression extends BaseTestCase {

  @Test
  public void test() {

    boolean expectNative = isExpectNative();

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
      .where().ilike("name", "rob")
      .query();

    List<Customer> list = query.findList();

    if (expectNative) {
      assertThat(query.getGeneratedSql()).contains(" from o_customer t0 where t0.name ilike ?");
      assertThat(list).isNotEmpty();
    }
  }

  private boolean isExpectNative() {

    SpiServer pluginApi = server().getPluginApi();
    boolean expressionNativeIlike = pluginApi.getServerConfig().isExpressionNativeIlike();
    Platform platform = pluginApi.getDatabasePlatform().getPlatform();

    return expressionNativeIlike && platform == Platform.POSTGRES;
  }
}
