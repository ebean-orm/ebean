package org.integration;

import io.ebean.DB;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheStatistics;
import io.ebeaninternal.server.cache.CachedBeanData;
import org.domain.*;
import org.domain.query.QOtherOne;
import org.domain.query.QPerson;
import org.domain.query.QRCust;
import org.domain.test.TestOne;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class IntegrationTest {

    private static OtherOne findOther(String a, String b) {
        return new QOtherOne()
            .one.eq(a)
            .two.eq(b)
            .findOne();
    }

    @Test
    void uuid_getPut() {
        UParent b0 = new UParent("b0");
        b0.children().add(new UChild(b0, "b0c0"));
        b0.children().add(new UChild(b0, "b0c1"));
        b0.save();

        ServerCache beanCache = DB.cacheManager().beanCache(UParent.class);
        beanCache.clear();
        beanCache.statistics(true);

        UParent found0 = DB.find(UParent.class, b0.id());
        assertThat(found0.name()).isEqualTo("b0");

        List<UChild> children = found0.children();
        assertThat(children).hasSize(2);

        UParent found1 = DB.find(UParent.class, b0.id());
        assertThat(found1.name()).isEqualTo("b0");

        DB.delete(found1);

        ServerCacheStatistics stats1 = beanCache.statistics(true);
        assertThat(stats1.getHitCount()).isEqualTo(1);
    }

    @Test
    void mget_when_emptyCollectionOfIds() {

        List<RCust> f0 = new QRCust()
            .setIdIn(Collections.emptyList())
            .findList();

        assertThat(f0).isEmpty();

        List<RCust> f1 = new QRCust()
            .id.in(Collections.emptyList())
            .findList();

        assertThat(f1).isEmpty();
    }

    @Test
    void mput_via_setIdIn() throws InterruptedException {

        ServerCache beanCache = DB.cacheManager().beanCache(RCust.class);
        beanCache.clear();
        beanCache.statistics(true);

        List<RCust> people = new ArrayList<>();
        for (String name : new String[]{"mp0", "mp1", "mp2"}) {
            people.add(new RCust(name));
        }
        DB.saveAll(people);
        List<Long> ids = people.stream().map(RCust::getId).collect(Collectors.toList());

        List<RCust> f0 = new QRCust()
            .setIdIn(ids) // using collection argument
            .findList();

        assertThat(f0).hasSize(3);
        ServerCacheStatistics stats0 = beanCache.statistics(true);
        assertThat(stats0.getHitCount()).isEqualTo(0);

        Thread.sleep(5);

        // we will hit the cache this time
        List<RCust> f1 = new QRCust()
            .setIdIn(ids.toArray()) // using varargs argument
            .findList();

        assertThat(f1).hasSize(3);
        ServerCacheStatistics stats1 = beanCache.statistics(true);
        assertThat(stats1.getHitCount()).isEqualTo(3);

        // we will hit the cache again
        List<RCust> f2 = new QRCust()
            .setIdIn(ids) // using collection argument
            .findList();
        assertThat(f2).hasSize(3);
        ServerCacheStatistics stats2 = beanCache.statistics(true);
        assertThat(stats2.getHitCount()).isEqualTo(3);
    }

    @Test
    void mput_via_propertyInExpression() throws InterruptedException {

        ServerCache beanCache = DB.cacheManager().beanCache(RCust.class);
        beanCache.clear();
        beanCache.statistics(true);

        List<RCust> people = new ArrayList<>();
        for (String name : new String[]{"mpx0", "mpx1", "mpx2"}) {
            people.add(new RCust(name));
        }
        DB.saveAll(people);
        List<Long> ids = people.stream().map(RCust::getId).collect(Collectors.toList());

        List<RCust> f0 = new QRCust()
            .id.in(ids)
            .findList();

        assertThat(f0).hasSize(3);
        ServerCacheStatistics stats0 = beanCache.statistics(true);
        assertThat(stats0.getHitCount()).isEqualTo(0);

        Thread.sleep(5);

        // we will hit the cache this time
        List<RCust> f1 = new QRCust()
            .id.in(ids)
            .findList();

        assertThat(f1).hasSize(3);
        ServerCacheStatistics stats1 = beanCache.statistics(true);
        assertThat(stats1.getHitCount()).isEqualTo(3);

        // we will hit the cache again
        List<RCust> f2 = new QRCust()
            .id.isIn(ids)
            .findList();
        assertThat(f2).hasSize(3);
        ServerCacheStatistics stats2 = beanCache.statistics(true);
        assertThat(stats2.getHitCount()).isEqualTo(3);
    }

    @Test
    void testOtherOne() {
        DB.save(new OtherOne("A", "B", "ab"));
        DB.save(new OtherOne("A", "C", "ac"));
        DB.save(new OtherOne("B", "B", "bb"));

        ServerCache nkeyCache = DB.cacheManager().naturalKeyCache(OtherOne.class);
        nkeyCache.clear();
        nkeyCache.statistics(true);

        OtherOne ab0 = findOther("A", "B");
        OtherOne ab1 = findOther("A", "B");
        OtherOne ab2 = findOther("A", "B");
        OtherOne bb = findOther("B", "B");

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

        Person fiona = findByName("Fiona");
        fiona.setName("Fortuna");
        fiona.setLocalDate(LocalDate.now());
        fiona.update();

        Thread.sleep(100);

        Person one = findById(1);
        assertThat(one).isNotNull();

        for (int i = 1; i < 4; i++) {
            System.out.println("hit " + findById(i));
        }

        List<Person> one2 = nameStartsWith("fo");
        assertThat(one2).hasSize(1);

        one2 = nameStartsWith("j");
        assertThat(one2).hasSize(2);

        one2 = nameStartsWith("j");
        assertThat(one2).hasSize(2);


        List<Person> byNames = findByNames("Jack", "Rob");
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

        DB.cacheManager().clear(Person.class);

        System.out.println("done");
    }

    private void insertSomePeople() {
        List<Person> people = new ArrayList<>();
        for (String name : new String[]{"Jack", "John", "Rob", "Moby", "Fiona"}) {
            people.add(new Person(name));
        }
        DB.saveAll(people);
    }

    private Person findByName(String name) {
        return new QPerson()
            .name.eq(name)
            .findOne();
    }

    private List<Person> findByNames(String... names) {
        return new QPerson()
            .name.in(names)
            .setUseCache(true)
            .findList();
    }

    private Person findById(int id) {
        return new QPerson()
            .id.eq(id)
            .findOne();
    }

    private List<Person> nameStartsWith(String pattern) {
        return new QPerson()
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
        ServerCache beanCache = DB.cacheManager().beanCache(RCust.class);
        beanCache.clear();

        RCust cust = new RCust("stale-test-orig");
        DB.save(cust);
        long id = cust.getId();

        // prime cache at version 1
        DB.find(RCust.class, id);
        Thread.sleep(5);
        Object staleV1 = beanCache.get(id);
        assertThat(staleV1).isNotNull();

        // update to version 2; ensure v2 is in cache
        cust.setName("stale-test-updated");
        DB.save(cust);
        DB.find(RCust.class, id);
        Thread.sleep(5);

        // stale write attempt: v2 is cached, v1 should be rejected
        beanCache.put(id, staleV1);

        // v2 must survive (Lua CAS blocked the stale v1 write)
        Object staleV2 = beanCache.get(id);
        assertThat(staleV2).isNotNull();
        assertThat(staleV2).isInstanceOf(CachedBeanData.class);
        assertThat(((CachedBeanData) staleV2).getVersion()).isEqualTo(2L);

        beanCache.statistics(true);
        RCust found = DB.find(RCust.class, id);
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
        ServerCache beanCache = DB.cacheManager().beanCache(TestOne.class);
        beanCache.clear();

        TestOne t1 = new TestOne("zvw-test", "unique-a");
        DB.save(t1);

        // prime cache at version 0 (no @Version field)
        DB.find(TestOne.class, "zvw-test");
        Thread.sleep(5);
        Object staleV0 = beanCache.get("zvw-test");
        assertThat(staleV0).isNotNull();

        // update; ensure new v0 is in cache
        t1.setOtherUnique("unique-b");
        DB.save(t1);
        DB.find(TestOne.class, "zvw-test");
        Thread.sleep(5);

        // stale write: v0 in cache, incoming v0 — must NOT be blocked (0 > 0 is false)
        beanCache.put("zvw-test", staleV0);

        // stale data should now be in cache (unlike the versioned case above)
        beanCache.statistics(true);
        TestOne found = DB.find(TestOne.class, "zvw-test");
        ServerCacheStatistics stats = beanCache.statistics(true);
        assertThat(stats.getHitCount()).isEqualTo(1);
        assertThat(found.getOtherUnique()).isEqualTo("unique-a");
    }
}
