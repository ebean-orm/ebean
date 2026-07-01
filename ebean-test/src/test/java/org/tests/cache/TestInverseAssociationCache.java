package org.tests.cache;

import io.ebean.CacheMode;
import io.ebean.DB;
import io.ebean.SqlUpdate;
import io.ebean.cache.ServerCache;
import io.ebean.xtest.BaseTestCase;
import io.ebeaninternal.server.cache.CachedManyIds;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.OCachedBean;
import org.tests.model.basic.OCachedBeanChild;
import org.tests.model.cache.o2o.OCachedO2ODetail;
import org.tests.model.cache.o2o.OCachedO2ODetailNoCached;
import org.tests.model.cache.o2o.OCachedO2OOwner;
import org.tests.model.cache.o2o.OCachedO2OOwnerNoCachedDetail;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that inverse association L2 caches are evicted when the owning side changes,
 * without evicting unrelated cache entries (precision), and that the fix is a no-op when
 * the target entity has no bean cache.
 * <ul>
 *   <li>collection-ids cache (parent) when a child's {@code @ManyToOne} FK is reassigned</li>
 *   <li>bean cache (detail) when an owning {@code @OneToOne} FK is inserted, updated, or deleted</li>
 * </ul>
 */
class TestInverseAssociationCache extends BaseTestCase {

  // ---- collection-ids cache: ManyToOne FK reassignment (reparenting) ----

  /**
   * When a child's ManyToOne FK is updated to point to a different parent (reparenting),
   * both the old and new parent's collection-ids caches must be evicted.
   */
  @Test
  void collectionIds_whenChildReparented_bothParentsEvicted() {
    OCachedBean p1 = new OCachedBean();
    p1.setName("p1-reparent");
    OCachedBean p2 = new OCachedBean();
    p2.setName("p2-reparent");
    DB.save(p1);
    DB.save(p2);

    OCachedBeanChild child = new OCachedBeanChild();
    child.setCachedBean(p1);
    DB.save(child);

    // populate both parents' collection-ids caches
    assertThat(DB.find(OCachedBean.class, p1.getId()).getChildren()).hasSize(1);
    assertThat(DB.find(OCachedBean.class, p2.getId()).getChildren()).isEmpty();

    // reparent the child from p1 to p2 (FK update) — ebean did not evict either cache before this fix
    OCachedBeanChild childRef = DB.find(OCachedBeanChild.class, child.getId());
    childRef.setCachedBean(DB.reference(OCachedBean.class, p2.getId()));
    DB.save(childRef);
    awaitL2Cache();

    assertThat(DB.find(OCachedBean.class, p1.getId()).getChildren())
      .as("old parent collection should be empty after reparent").isEmpty();
    assertThat(DB.find(OCachedBean.class, p2.getId()).getChildren())
      .as("new parent collection should contain the moved child").hasSize(1);

    // cleanup
    DB.delete(childRef);
    DB.delete(p1);
    DB.delete(p2);
  }

  /**
   * Precision: reparenting a child only evicts the old and new parent's collection-ids entry;
   * a third parent's collection-ids cache entry must remain untouched.
   */
  @Test
  void collectionIds_whenChildReparented_unrelatedParentCacheUnaffected() {
    OCachedBean p1 = new OCachedBean();
    p1.setName("p1-prec");
    OCachedBean p2 = new OCachedBean();
    p2.setName("p2-prec");
    OCachedBean p3 = new OCachedBean();
    p3.setName("p3-prec");
    DB.save(p1);
    DB.save(p2);
    DB.save(p3);

    OCachedBeanChild childOfP1 = new OCachedBeanChild();
    childOfP1.setCachedBean(p1);
    OCachedBeanChild childOfP3 = new OCachedBeanChild();
    childOfP3.setCachedBean(p3);
    DB.save(childOfP1);
    DB.save(childOfP3);

    // populate all three collection caches
    assertThat(DB.find(OCachedBean.class, p1.getId()).getChildren()).hasSize(1);
    assertThat(DB.find(OCachedBean.class, p2.getId()).getChildren()).isEmpty();
    assertThat(DB.find(OCachedBean.class, p3.getId()).getChildren()).hasSize(1);

    // verify p3's cache entry is actually present before the reparent
    ServerCache collCache = DB.cacheManager().collectionIdsCache(OCachedBean.class, "children");
    CachedManyIds p3Before = (CachedManyIds) collCache.get(String.valueOf(p3.getId()));
    assertThat(p3Before).as("p3 collection-ids cache populated before reparent").isNotNull();

    // reparent childOfP1 from p1 to p2 (only p1 and p2 should be affected)
    OCachedBeanChild childRef = DB.find(OCachedBeanChild.class, childOfP1.getId());
    childRef.setCachedBean(DB.reference(OCachedBean.class, p2.getId()));
    DB.save(childRef);
    awaitL2Cache();

    // p3's collection-ids cache entry must still be present (only p1 and p2 were evicted)
    CachedManyIds p3After = (CachedManyIds) collCache.get(String.valueOf(p3.getId()));
    assertThat(p3After).as("p3 collection-ids cache must not be evicted by unrelated reparent").isNotNull();

    // cleanup
    DB.delete(childRef);
    DB.delete(childOfP3);
    DB.delete(p1);
    DB.delete(p2);
    DB.delete(p3);
  }

  // ---- exported OneToOne: bean cache eviction ----

  /**
   * When a new owning OneToOne bean is inserted pointing at a detail, the detail's
   * bean cache must be evicted so the next read reflects the newly established link.
   */
  @Test
  void oneToOne_whenOwnerInserted_detailBeanCacheEvicted() {
    OCachedO2ODetail d = new OCachedO2ODetail("d-ins");
    DB.save(d);
    long dId = d.getId();

    // populate detail's bean cache with owner=null
    DB.find(OCachedO2ODetail.class).setId(dId).fetch("owner").setBeanCacheMode(CacheMode.PUT).findOne();
    assertThat(DB.find(OCachedO2ODetail.class, dId).getOwner()).as("initially no owner").isNull();

    // insert a new owner pointing at d — must evict d's bean cache
    OCachedO2OOwner newOwner = new OCachedO2OOwner("o-ins", DB.reference(OCachedO2ODetail.class, dId));
    DB.save(newOwner);
    awaitL2Cache();

    OCachedO2OOwner ownerAfter = DB.find(OCachedO2ODetail.class, dId).getOwner();
    assertThat(ownerAfter).as("detail owner should be present after owner insert").isNotNull();
    assertThat(ownerAfter.getId()).isEqualTo(newOwner.getId());

    // cleanup
    DB.delete(newOwner);
    DB.delete(d);
  }

  /**
   * When the owning OneToOne FK is changed to point to a different detail (repoint),
   * both the old detail's and new detail's bean caches must be evicted.
   */
  @Test
  void oneToOne_whenOwnerFkRepointed_bothDetailBeanCachesEvicted() {
    OCachedO2ODetail d1 = new OCachedO2ODetail("d1-repoint");
    OCachedO2ODetail d2 = new OCachedO2ODetail("d2-repoint");
    DB.save(d1);
    DB.save(d2);
    OCachedO2OOwner owner = new OCachedO2OOwner("o-repoint", d1);
    DB.save(owner);
    long d1Id = d1.getId();
    long d2Id = d2.getId();

    // populate both details' bean caches: d1 with owner reference, d2 with owner=null
    DB.find(OCachedO2ODetail.class).setId(d1Id).fetch("owner").setBeanCacheMode(CacheMode.PUT).findOne();
    DB.find(OCachedO2ODetail.class).setId(d2Id).fetch("owner").setBeanCacheMode(CacheMode.PUT).findOne();
    assertThat(DB.find(OCachedO2ODetail.class, d1Id).getOwner()).as("d1 initially owned").isNotNull();
    assertThat(DB.find(OCachedO2ODetail.class, d2Id).getOwner()).as("d2 initially has no owner").isNull();

    // repoint owner from d1 to d2 — must evict both d1's and d2's bean caches
    OCachedO2OOwner ownerRef = DB.find(OCachedO2OOwner.class, owner.getId());
    ownerRef.setDetail(DB.reference(OCachedO2ODetail.class, d2Id));
    DB.save(ownerRef);
    awaitL2Cache();

    assertThat(DB.find(OCachedO2ODetail.class, d1Id).getOwner())
      .as("d1 owner after repoint (should be null)").isNull();
    assertThat(DB.find(OCachedO2ODetail.class, d2Id).getOwner())
      .as("d2 owner after repoint (should be the owner)").isNotNull();

    // cleanup
    DB.delete(ownerRef);
    DB.delete(d1);
    DB.delete(d2);
  }

  /**
   * Precision: repointng only evicts the old and new detail's bean cache entry;
   * a third, unrelated detail's bean cache entry must remain untouched.
   */
  @Test
  void oneToOne_whenOwnerFkRepointed_unrelatedDetailCacheUnaffected() {
    OCachedO2ODetail d1 = new OCachedO2ODetail("d1-prec");
    OCachedO2ODetail d2 = new OCachedO2ODetail("d2-prec");
    OCachedO2ODetail d3 = new OCachedO2ODetail("d3-prec");
    DB.save(d1);
    DB.save(d2);
    DB.save(d3);
    OCachedO2OOwner owner = new OCachedO2OOwner("o-prec", d1);
    DB.save(owner);
    long d1Id = d1.getId();
    long d2Id = d2.getId();
    long d3Id = d3.getId();

    // populate all three details in bean cache
    DB.find(OCachedO2ODetail.class).setId(d1Id).fetch("owner").setBeanCacheMode(CacheMode.PUT).findOne();
    DB.find(OCachedO2ODetail.class).setId(d2Id).fetch("owner").setBeanCacheMode(CacheMode.PUT).findOne();
    DB.find(OCachedO2ODetail.class).setId(d3Id).fetch("owner").setBeanCacheMode(CacheMode.PUT).findOne();

    // confirm d3 is in bean cache before the repoint
    ServerCache beanCache = DB.cacheManager().beanCache(OCachedO2ODetail.class);
    assertThat(beanCache.get(String.valueOf(d3Id))).as("d3 bean cache populated before repoint").isNotNull();

    // repoint owner from d1 to d2 (only d1 and d2 should be evicted from bean cache)
    OCachedO2OOwner ownerRef = DB.find(OCachedO2OOwner.class, owner.getId());
    ownerRef.setDetail(DB.reference(OCachedO2ODetail.class, d2Id));
    DB.save(ownerRef);
    awaitL2Cache();

    // d3's bean cache entry must still be present
    assertThat(beanCache.get(String.valueOf(d3Id)))
      .as("d3 bean cache must not be evicted by unrelated repoint").isNotNull();

    // cleanup
    DB.delete(ownerRef);
    DB.delete(d1);
    DB.delete(d2);
    DB.delete(d3);
  }

  /**
   * When an owning OneToOne bean is deleted (by bean), the detail's bean cache
   * must be evicted so the next read reflects that the link is gone.
   */
  @Test
  void oneToOne_whenOwnerDeletedByBean_detailBeanCacheEvicted() {
    OCachedO2ODetail d = new OCachedO2ODetail("d-del-bean");
    DB.save(d);
    OCachedO2OOwner owner = new OCachedO2OOwner("o-del-bean", d);
    DB.save(owner);
    long dId = d.getId();

    // populate detail's bean cache with the owner reference
    DB.find(OCachedO2ODetail.class).setId(dId).fetch("owner").setBeanCacheMode(CacheMode.PUT).findOne();
    assertThat(DB.find(OCachedO2ODetail.class, dId).getOwner()).as("initially owned").isNotNull();

    // delete the owner by bean — must evict d's bean cache
    DB.delete(DB.find(OCachedO2OOwner.class, owner.getId()));
    awaitL2Cache();

    assertThat(DB.find(OCachedO2ODetail.class, dId).getOwner())
      .as("detail owner should be null after owner delete (by bean)").isNull();

    // cleanup
    DB.delete(d);
  }

  /**
   * When an owning OneToOne bean is deleted by id (no bean loaded), the detail's bean
   * cache must be cleared so the next read reflects that the link is gone.
   */
  @Test
  void oneToOne_whenOwnerDeletedById_detailBeanCacheCleared() {
    OCachedO2ODetail d = new OCachedO2ODetail("d-del-id");
    DB.save(d);
    OCachedO2OOwner owner = new OCachedO2OOwner("o-del-id", d);
    DB.save(owner);
    long dId = d.getId();

    // populate detail's bean cache with the owner reference
    DB.find(OCachedO2ODetail.class).setId(dId).fetch("owner").setBeanCacheMode(CacheMode.PUT).findOne();
    assertThat(DB.find(OCachedO2ODetail.class, dId).getOwner()).as("initially owned").isNotNull();

    // delete the owner by id (no bean loaded) — must clear d's entire bean cache
    DB.delete(OCachedO2OOwner.class, owner.getId());
    awaitL2Cache();

    assertThat(DB.find(OCachedO2ODetail.class, dId).getOwner())
      .as("detail owner should be null after owner delete (by id)").isNull();

    // cleanup
    DB.delete(d);
  }

  // ---- non-cached target: no errors, no unintended eviction ----

  /**
   * When the target detail has no {@code @Cache}, {@code cacheNotifyOwningOneToOne} is false
   * and all persist operations are no-ops for cache purposes — no exception, no unintended clearing.
   */
  @Test
  void oneToOne_withNonCachedDetail_insertUpdateDelete_noErrors() {
    OCachedO2ODetailNoCached d = new OCachedO2ODetailNoCached("d-nc");
    DB.save(d);

    // insert owner → no cache eviction, no error
    OCachedO2OOwnerNoCachedDetail owner = new OCachedO2OOwnerNoCachedDetail("o-nc", d);
    DB.save(owner);

    // update owner FK → no cache eviction, no error
    OCachedO2ODetailNoCached d2 = new OCachedO2ODetailNoCached("d2-nc");
    DB.save(d2);
    OCachedO2OOwnerNoCachedDetail ownerRef = DB.find(OCachedO2OOwnerNoCachedDetail.class, owner.getId());
    ownerRef.setDetail(d2);
    DB.save(ownerRef);

    // delete owner → no cache eviction, no error
    DB.delete(ownerRef);

    // delete-by-id → no cache eviction, no error
    OCachedO2OOwnerNoCachedDetail o2 = new OCachedO2OOwnerNoCachedDetail("o2-nc", d2);
    DB.save(o2);
    DB.delete(OCachedO2OOwnerNoCachedDetail.class, o2.getId());

    // verify the cached detail's bean cache is NOT affected by the non-cached owner operations
    OCachedO2ODetail cachedDetail = new OCachedO2ODetail("still-cached");
    DB.save(cachedDetail);
    DB.find(OCachedO2ODetail.class).setId(cachedDetail.getId()).setBeanCacheMode(CacheMode.PUT).findOne();
    ServerCache beanCache = DB.cacheManager().beanCache(OCachedO2ODetail.class);
    assertThat(beanCache.get(String.valueOf(cachedDetail.getId())))
      .as("unrelated cached detail's bean cache must not be wiped by non-cached owner operations").isNotNull();

    // cleanup
    DB.delete(cachedDetail);
    DB.delete(d);
    DB.delete(d2);
  }

  // ---- SQL/bulk update path (persistTableIUD) ----

  /**
   * When the owner table is updated via a raw SQL update, the detail's bean cache
   * must be cleared because we cannot know which FKs changed.
   */
  @Test
  void oneToOne_whenOwnerTableBulkUpdated_detailBeanCacheCleared() {
    OCachedO2ODetail d = new OCachedO2ODetail("d-sql");
    DB.save(d);
    OCachedO2OOwner owner = new OCachedO2OOwner("o-sql", d);
    DB.save(owner);
    long dId = d.getId();

    // populate detail's bean cache with owner reference
    DB.find(OCachedO2ODetail.class).setId(dId).fetch("owner").setBeanCacheMode(CacheMode.PUT).findOne();
    assertThat(DB.find(OCachedO2ODetail.class, dId).getOwner()).as("initially owned").isNotNull();

    // raw SQL update on the owner table — triggers persistTableIUD which must clear detail's bean cache
    SqlUpdate update = DB.sqlUpdate("update o_cached_o2o_owner set name = 'sql-updated' where id = :id");
    update.setParameter("id", owner.getId());
    update.execute();
    awaitL2Cache();

    // detail's bean cache should have been cleared (entire cache, since we don't know which FKs changed)
    ServerCache beanCache = DB.cacheManager().beanCache(OCachedO2ODetail.class);
    assertThat(beanCache.get(String.valueOf(dId)))
      .as("detail bean cache should be cleared after bulk SQL update on owner table").isNull();

    // cleanup
    DB.delete(owner);
    DB.delete(d);
  }
}
