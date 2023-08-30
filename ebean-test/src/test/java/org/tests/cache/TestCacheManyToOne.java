package org.tests.cache;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.OCachedInhRoot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class testing deleting/invalidating of cached beans
 */
public class TestCacheManyToOne extends BaseTestCase {

  @Test
  public void crudInvoice() {

    OCachedInhRoot bean = new OCachedInhRoot();
    bean.setName("Roland");
    bean.setChildAData("ChildData");
    DB.save(bean);

    DB.find(OCachedInhRoot.class).select("id").where().idIn(bean.getId()).findEach(bean0 -> {
      assertNotNull(bean0);
      assertThat(bean0.getName()).isEqualTo("Roland");
      assertThat((bean0).getChildAData()).isEqualTo("ChildData");
    });

    DB.find(OCachedInhRoot.class).select("id").where().idIn(bean.getId()).findEach(bean1 -> {
      assertNotNull(bean1);
      assertThat(bean1.getName()).isEqualTo("Roland");
      // this will fail if the bean is loaded from cache and the wrong discriminator is used.
      assertThat((bean1).getChildAData()).isEqualTo("ChildData");
    });

  }
}
