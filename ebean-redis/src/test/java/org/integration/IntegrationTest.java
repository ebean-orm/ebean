package org.integration;

import io.ebean.DB;
import org.domain.Person;
import org.domain.query.QPerson;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationTest {

  @Test
  public void test() throws InterruptedException {

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

    DB.getServerCacheManager().clear(Person.class);

    System.out.println("done");
  }

  private List<Person> insertSomePeople() {

    List<Person> people = new ArrayList<>();
    for (String name : new String[]{"Jack", "John", "Rob", "Moby", "Fiona"}) {
      people.add(new Person(name));
    }

    DB.saveAll(people);
    return people;
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
