package org.integration;

import io.ebean.DB;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheStatistics;
import io.ebeaninternal.server.cache.CachedBeanData;
import org.domain.*;
import org.domain.query.QFOtherOne;
import org.domain.query.QFPerson;
import org.domain.query.QFRCust;
import org.domain.test.RTestOne;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class IntegrationTest {

    private static FOtherOne findOther(String a, String b) {
        return new QFOtherOne()
            .one.eq(a)
            .two.eq(b)
            .findOne();
    }

    @Test
    void uuid_getPut() {
        FUParent b0 = new FUParent("b0");
        b0.children().add(new FUChild(b0, "b0c0"));
        b0.children().add(new FUChild(b0, "b0c1"));
        b0.save();

        ServerCache beanCache = DB.cacheManager().beanCache(FUParent.class);
        beanCache.clear();
        beanCache.statistics(true);

        FUParent found0 = DB.find(FUParent.class, b0.id());
        assertThat(found0.name()).isEqualTo("b0");

        List<FUChild> children = found0.children();
        assertThat(children).hasSize(2);

        FUParent found1 = DB.find(FUParent.class, b0.id());
        assertThat(found1.name()).isEqualTo("b0");

        DB.delete(found1);

        ServerCacheStatistics stats1 = beanCache.statistics(true);
        assertThat(stats1.getHitCount()).isEqualTo(1);
    }

    @Test
    void mget_when_emptyCollectionOfIds() {

        List<FRCust> f0 = new QFRCust()
            .setIdIn(Collections.emptyList())
            .findList();

        assertThat(f0).isEmpty();

        List<FRCust> f1 = new QFRCust()
            .id.in(Collections.emptyList())
            .findList();

        assertThat(f1).isEmpty();
    }

    @Test
    void mput_via_setIdIn() throws InterruptedException {

        ServerCache beanCache = DB.cacheManager().beanCache(FRCust.class);
        beanCache.clear();
        beanCache.statistics(true);

        List<FRCust> people = new ArrayList<>();
        for (String name : new String[]{"mp0", "mp1", "mp2"}) {
            people.add(new FRCust(name));
        }
        DB.saveAll(people);
        List<Long> ids = people.stream().map(FRCust::getId).collect(Collectors.toList());

        List<FRCust> f0 = new QFRCust()
            .setIdIn(ids) // using collection argument
            .findList();

        assertThat(f0).hasSize(3);
        ServerCacheStatistics stats0 = beanCache.statistics(true);
        assertThat(stats0.getHitCount()).isEqualTo(0);

        Thread.sleep(5);

        // we will hit the cache this time
        List<FRCust> f1 = new QFRCust()
            .setIdIn(ids.toArray()) // using varargs argument
            .findList();

        assertThat(f1).hasSize(3);
        ServerCacheStatistics stats1 = beanCache.statistics(true);
        assertThat(stats1.getHitCount()).isEqualTo(3);

        // we will hit the cache again
        List<FRCust> f2 = new QFRCust()
            .setIdIn(ids) // using collection argument
            .findList();
        assertThat(f2).hasSize(3);
        ServerCacheStatistics stats2 = beanCache.statistics(true);
        assertThat(stats2.getHitCount()).isEqualTo(3);
    }

    @Test
    void mput_via_propertyInExpression() throws InterruptedException {

        ServerCache beanCache = DB.cacheManager().beanCache(FRCust.class);
        beanCache.clear();
        beanCache.statistics(true);

        List<FRCust> people = new ArrayList<>();
        for (String name : new String[]{"mpx0", "mpx1", "mpx2"}) {
            people.add(new FRCust(name));
        }
        DB.saveAll(people);
        List<Long> ids = people.stream().map(FRCust::getId).collect(Collectors.toList());

        List<FRCust> f0 = new QFRCust()
            .id.in(ids)
            .findList();

        assertThat(f0).hasSize(3);
        ServerCacheStatistics stats0 = beanCache.statistics(true);
        assertThat(stats0.getHitCount()).isEqualTo(0);

        Thread.sleep(5);

        // we will hit the cache this time
        List<FRCust> f1 = new QFRCust()
            .id.in(ids)
            .findList();

        assertThat(f1).hasSize(3);
        ServerCacheStatistics stats1 = beanCache.statistics(true);
        assertThat(stats1.getHitCount()).isEqualTo(3);

        // we will hit the cache again
        List<FRCust> f2 = new QFRCust()
            .id.isIn(ids)
            .findList();
        assertThat(f2).hasSize(3);
        ServerCacheStatistics stats2 = beanCache.statistics(true);
        assertThat(stats2.getHitCount()).isEqualTo(3);
    }

    @Test
    void testOtherOne() {
        DB.save(new FOtherOne("A", "B", "ab"));
        DB.save(new FOtherOne("A", "C", "ac"));
        DB.save(new FOtherOne("B", "B", "bb"));

        ServerCache nkeyCache = DB.cacheManager().naturalKeyCache(FOtherOne.class);
        nkeyCache.clear();
        nkeyCache.statistics(true);

        FOtherOne ab0 = findOther("A", "B");
        FOtherOne ab1 = findOther("A", "B");
        FOtherOne ab2 = findOther("A", "B");
        FOtherOne bb = findOther("B", "B");

        assertThat(ab0).isNotNull();
        assertThat(ab1).isNotNull();
        assertThat(ab2).isNotNull();
        assertThat(bb).isNotNull();

        ServerCacheStatistics statistics = nkeyCache.statistics(true);
        assertThat(statistics.getHitCount()).isEqualTo(2);
    }

    @Test
    void test() throws InterruptedException {

        insertSomePeople();

        FPerson fiona = findByName("Fiona");
        fiona.setName("Fortuna");
        fiona.setLocalDate(LocalDate.now());
        fiona.update();

        Thread.sleep(100);

        FPerson one = findById(1);
        assertThat(one).isNotNull();

        for (int i = 1; i < 4; i++) {
            System.out.println("hit " + findById(i));
        }

        List<FPerson> one2 = nameStartsWith("fo");
        assertThat(one2).hasSize(1);

        one2 = nameStartsWith("j");
        assertThat(one2).hasSize(2);

        one2 = nameStartsWith("j");
        assertThat(one2).hasSize(2);


        List<FPerson> byNames = findByNames("Jack", "Rob");
        assertThat(byNames).hasSize(2);

        byNames = findByNames("Jack", "Rob", "Moby");
        assertThat(byNames).hasSize(3);

        fiona.setName("fo2");
        fiona.setLocalDate(LocalDate.now());
        fiona.update();

        byNames = findByNames("Jack", "Rob", "Moby");
        assertThat(byNames).hasSize(3);

        Thread.sleep(200);

        one2 = nameStartsWith("fo%");
        System.out.println("one2 " + one2);
        one2 = nameStartsWith("f0%");

        System.out.println("one2 " + one2);

        DB.cacheManager().clear(FPerson.class);

        System.out.println("done");
    }

    private void insertSomePeople() {
        List<FPerson> people = new ArrayList<>();
        for (String name : new String[]{"Jack", "John", "Rob", "Moby", "Fiona"}) {
            people.add(new FPerson(name));
        }
        DB.saveAll(people);
    }

    private FPerson findByName(String name) {
        return new QFPerson()
            .name.eq(name)
            .findOne();
    }

    private List<FPerson> findByNames(String... names) {
        return new QFPerson()
            .name.in(names)
            .setUseCache(true)
            .findList();
    }

    private FPerson findById(int id) {
        return new QFPerson()
            .id.eq(id)
            .findOne();
    }

    private List<FPerson> nameStartsWith(String pattern) {
        return new QFPerson()
            .name.istartsWith(pattern)
            .setUseQueryCache(true)
            .findList();
    }

    /**
     * Verifies the Lua CAS: a stored version 2 must NOT be overwritten by an incoming version 1.
     * Strict greater-than comparison (stored > incoming → skip) ensures stale cluster writes are ignored.
     */
    @Test
    void versionGated_newerCached_staleWriteIsIgnored() throws InterruptedException {
        ServerCache beanCache = DB.cacheManager().beanCache(FRCust.class);
        beanCache.clear();

        FRCust cust = new FRCust("stale-test-orig");
        DB.save(cust);
        long id = cust.getId();

        // prime cache at version 1
        DB.find(FRCust.class, id);
        Thread.sleep(150);
        Object staleV1 = beanCache.get(id);
        assertThat(staleV1).isNotNull();

        // update to version 2; ensure v2 is in cache
        cust.setName("stale-test-updated");
        DB.save(cust);
        DB.find(FRCust.class, id);
        Thread.sleep(150);

        // stale write attempt: v2 is cached, v1 should be rejected
        beanCache.put(id, staleV1);

        // v2 must survive (Lua CAS blocked the stale v1 write)
        Object staleV2 = beanCache.get(id);
        assertThat(staleV2).isNotNull();
        assertThat(staleV2).isInstanceOf(CachedBeanData.class);
        assertThat(((CachedBeanData) staleV2).getVersion()).isEqualTo(2L);

        beanCache.statistics(true);
        FRCust found = DB.find(FRCust.class, id);
        ServerCacheStatistics stats = beanCache.statistics(true);
        assertThat(stats.getHitCount()).isEqualTo(1);
        assertThat(found.getVersion()).isEqualTo(2L);
        assertThat(found.getName()).isEqualTo("stale-test-updated");
    }

    /**
     * Verifies that for beans without {@code @Version} the version is always 0.
     * Equal-version comparison (stored v0 > incoming v0 is false) must never block a write,
     * so cache updates always go through for unversioned beans.
     */
    @Test
    void zeroVersion_equalVersion_staleWriteIsNotBlocked() throws InterruptedException {
        ServerCache beanCache = DB.cacheManager().beanCache(RTestOne.class);
        beanCache.clear();

        RTestOne t1 = new RTestOne("zvw-test", "unique-a");
        DB.save(t1);

        // prime cache at version 0 (no @Version field)
        DB.find(RTestOne.class, "zvw-test");
        Thread.sleep(150);
        Object staleV0 = beanCache.get("zvw-test");
        assertThat(staleV0).isNotNull();

        // update; ensure new v0 is in cache
        t1.setOtherUnique("unique-b");
        DB.save(t1);
        DB.find(RTestOne.class, "zvw-test");
        Thread.sleep(150);

        // stale write: v0 in cache, incoming v0 — must NOT be blocked (0 > 0 is false)
        beanCache.put("zvw-test", staleV0);

        // stale data should now be in cache (unlike the versioned case above)
        beanCache.statistics(true);
        RTestOne found = DB.find(RTestOne.class, "zvw-test");
        ServerCacheStatistics stats = beanCache.statistics(true);
        assertThat(stats.getHitCount()).isEqualTo(1);
        assertThat(found.getOtherUnique()).isEqualTo("unique-a");
    }
}
