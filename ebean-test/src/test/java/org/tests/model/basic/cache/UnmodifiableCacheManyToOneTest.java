package org.tests.model.basic.cache;

import io.ebean.DB;
import io.ebean.bean.EntityBean;
import io.ebean.bean.InterceptReadOnly;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reproduces the NPE when loading a bean-cached entity that has a @ManyToOne
 * via an unmodifiable id lookup (as the ImmutableBeanCache query loader does).
 */
public class UnmodifiableCacheManyToOneTest extends BaseTestCase {

  @Test
  public void unmodifiable_idIn_findMap_withManyToOne_fromBeanCache() {
    OCachedApp app = new OCachedApp("appNpe");
    app.save();
    OCachedAppDetail d0 = new OCachedAppDetail(app, "npe0");
    OCachedAppDetail d1 = new OCachedAppDetail(app, "npe1");
    d0.save();
    d1.save();

    List<Long> ids = List.of(d0.getId(), d1.getId());

    clearAllL2Cache();
    // populate the L2 bean cache
    DB.find(OCachedAppDetail.class).setUseCache(true).where().idIn(ids).findList();

    // mimic ImmutableBeanCaches.QueryLoader: unmodifiable id lookup -> bean cache hit
    Map<Long, OCachedAppDetail> map = DB.find(OCachedAppDetail.class)
      .setUnmodifiable(true)
      .where().idIn(ids)
      .findMap();

    assertThat(map).hasSize(2);
    OCachedAppDetail found = map.get(d0.getId());
    assertThat(found).isNotNull();

    OCachedApp appRef = found.getApp();
    assertThat(appRef).isNotNull();
    assertThat(appRef.getId()).isEqualTo(app.getId());

    // the @ManyToOne reference must be unmodifiable so the bean graph can be frozen
    assertThat(((EntityBean) appRef)._ebean_getIntercept()).isInstanceOf(InterceptReadOnly.class);
    assertThat(((EntityBean) found)._ebean_getIntercept()).isInstanceOf(InterceptReadOnly.class);
  }
}
