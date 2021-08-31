package org.tests.cache;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.tests.model.cache.M2MCacheChild;
import org.tests.model.cache.M2MCacheMaster;

import io.ebean.BaseTestCase;
import io.ebean.DB;

public class TestM2MCache extends BaseTestCase {

  @Test
  public void testM2MWithCache() throws Exception {
    M2MCacheChild cld = new M2MCacheChild();
    cld.setName("test");
    cld.setId(1);
    DB.save(cld);

    M2MCacheMaster cfg = new M2MCacheMaster();
    cfg.setId(42);
    cfg.getSet1().add(cld);
    cfg.getSet2().add(cld);
    DB.save(cfg);

    // find master and access set1 + set2.
    // lazy load something from set1
    M2MCacheMaster cfg1 = DB.find(M2MCacheMaster.class, 42);

    cfg1.getSet1().size();
    cfg1.getSet2().size();
    assertThat(cfg1.getSet1().iterator().next().getName()).isEqualTo("test");

    // do it again
    cfg1 = DB.find(M2MCacheMaster.class, 42);

    cfg1.getSet1().size();
    cfg1.getSet2().size();

    assertThat(cfg1.getSet1().iterator().next().getName()).isEqualTo("test");
    // do it again
    cfg1 = DB.find(M2MCacheMaster.class, 42);

    cfg1.getSet1().size();
    cfg1.getSet2().size();

    assertThat(cfg1.getSet1().iterator().next().getName()).isEqualTo("test");

  }

  @Test
  public void testM2MWithCacheMinimal() throws Exception {
    M2MCacheChild cld = new M2MCacheChild();
    cld.setName("test");
    cld.setId(2);
    DB.save(cld);

    M2MCacheMaster cfg = new M2MCacheMaster();
    cfg.setId(43);
    cfg.getSet1().add(cld);
    cfg.getSet2().add(cld);
    DB.save(cfg);

    DB.find(M2MCacheMaster.class, 43);

    M2MCacheMaster cfg1 = DB.find(M2MCacheMaster.class, 43);
    cfg1.getSet2().size();

    cfg1 = DB.find(M2MCacheMaster.class, 43);

    cfg1.getSet1().size();
    cfg1.getSet2().size();

    assertThat(cfg1.getSet1().iterator().next().getName()).isEqualTo("test");

  }
}
