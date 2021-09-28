package org.integration;

import io.ebean.DB;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheStatistics;
import org.domain.Person;
import org.domain.RCust;
import org.domain.query.QPerson;
import org.domain.query.QRCust;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class IntegrationTest {

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
}
