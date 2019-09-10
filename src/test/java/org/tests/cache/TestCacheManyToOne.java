package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.OCachedInhChildA;
import org.tests.model.basic.OCachedInhRoot;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * Test class testing deleting/invalidating of cached beans
 */
public class TestCacheManyToOne extends BaseTestCase {

  @Test
  public void crudInvoice() {

    OCachedInhChildA bean = new OCachedInhChildA();
    bean.setName("Roland");
    bean.setChildAData("ChildData");
    Ebean.save(bean);

    Ebean.find(OCachedInhRoot.class).select("id").where().idIn(bean.getId()).findEach(bean0 -> {
      assertNotNull(bean0);
      assertThat(bean0.getName()).isEqualTo("Roland");
      assertThat(((OCachedInhChildA)bean0).getChildAData()).isEqualTo("ChildData");
    });

    Ebean.find(OCachedInhRoot.class).select("id").where().idIn(bean.getId()).findEach(bean1 -> {
      assertNotNull(bean1);
      assertThat(bean1.getName()).isEqualTo("Roland");
      // this will fail if the bean is loaded from cache and the wrong discriminator is used.
      assertThat(((OCachedInhChildA)bean1).getChildAData()).isEqualTo("ChildData");
    });

  }
}
