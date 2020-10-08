package org.tests.model.basic.cache;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNatKeyCacheWhenNull extends BaseTestCase {

  public static final String WILL_CHANGE_TO_NULL = "WillChangeToNull";

  @Test
  public void updateToNull() {

    OCachedApp app = setup();

    // act
    app.setAppName(null);
    app.save();

    final OCachedApp foundAfter = DB.find(OCachedApp.class)
      .where().eq("appName", WILL_CHANGE_TO_NULL)
      .findOne();

    assertThat(foundAfter).isNull();
    app.delete();
  }

  private OCachedApp setup() {
    OCachedApp app = new OCachedApp(WILL_CHANGE_TO_NULL);
    app.save();

    final OCachedApp foundBefore = DB.find(OCachedApp.class)
      .where().eq("appName", WILL_CHANGE_TO_NULL)
      .findOne();

    assertThat(foundBefore).isNotNull();
    return app;
  }
}
