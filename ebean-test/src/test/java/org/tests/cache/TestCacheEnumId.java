package org.tests.cache;

import io.ebean.DB;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.ECachedEnumId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for #3110 - Finder.byId() on a @Cache entity with an Enum @Id (mapped via
 * @EnumValue) threw a NumberFormatException on the second call (cache hit) because
 * the bean cache key (the enum's name()) was being converted back to the bean id
 * using the db-value based toBeanType() rather than parse() (the correct inverse
 * of the cache key's format()).
 */
class TestCacheEnumId extends BaseTestCase {

  @Test
  void findById_enumId_whenCacheHit() {
    ECachedEnumId bean = new ECachedEnumId();
    bean.setStatus(ECachedEnumId.Status.APPROVED);
    bean.setName("approved");
    DB.save(bean);

    // first find - misses the bean cache, loads from DB and populates the cache
    ECachedEnumId bean0 = DB.find(ECachedEnumId.class, ECachedEnumId.Status.APPROVED);
    assertThat(bean0).isNotNull();
    assertThat(bean0.getStatus()).isEqualTo(ECachedEnumId.Status.APPROVED);

    // second find - hits the bean cache, used to throw NumberFormatException
    ECachedEnumId bean1 = DB.find(ECachedEnumId.class, ECachedEnumId.Status.APPROVED);
    assertThat(bean1).isNotNull();
    assertThat(bean1.getStatus()).isEqualTo(ECachedEnumId.Status.APPROVED);
    assertThat(bean1.getName()).isEqualTo("approved");
  }
}
