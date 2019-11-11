package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Dog;

import java.sql.Date;
import java.time.MonthDay;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestRawSelect extends BaseTestCase {

  @BeforeClass
  public static void setup() {
    Dog dog = new Dog();
    dog.setRegistrationNumber("RawSelect1");
    dog.setDateOfBirth(Date.valueOf("2018-07-04"));
    Ebean.save(dog);
    dog = new Dog();
    dog.setRegistrationNumber("RawSelect2");
    dog.setDateOfBirth(Date.valueOf("2016-11-22"));
    Ebean.save(dog);
    dog = new Dog();
    dog.setRegistrationNumber("RawSelect3");
    dog.setDateOfBirth(Date.valueOf("2017-08-03"));
    Ebean.save(dog);

    Customer customer = new Customer();
    customer.setName("RawSelect1");
    customer.setAnniversary(Date.valueOf("2018-07-04"));
    Ebean.save(customer);
    customer = new Customer();
    customer.setName("RawSelect2");
    customer.setAnniversary(Date.valueOf("2016-11-22"));
    Ebean.save(customer);
    customer = new Customer();
    customer.setName("RawSelect3");
    customer.setAnniversary(Date.valueOf("2017-08-03"));
    Ebean.save(customer);
  }

  @AfterClass
  public static void teardown() {
    Ebean.find(Dog.class).where().startsWith("registrationNumber", "RawSelect").delete();
    Ebean.find(Customer.class).where().startsWith("name", "RawSelect").delete();
  }

  @Test
  public void testFindSingleAttributeInherit() {

    Query<Dog> query = Ebean.find(Dog.class);
    query.select("cast(concat('2000-',month(dateOfBirth),'-', day(dateOfBirth)) as date)::MonthDay as birthday")
    .where().startsWith("registrationNumber", "RawSelect")
    .order("birthday");

    List<MonthDay> list = query.findSingleAttributeList();


    String sql = sqlOf(query, 5);
    //assertThat(sql).contains("select cast(concat('2000-',month(t0.date_of_birth),'-', day(t0.date_of_birth)) as date) birthday from animal t0 where t0.species in ('DOG','BDG')  order by birthday");


    assertThat(list.get(0)).isEqualTo(MonthDay.of(7,4));
    assertThat(list.get(1)).isEqualTo(MonthDay.of(8,3));
    assertThat(list.get(2)).isEqualTo(MonthDay.of(11,22));
  }

  @Test
  public void testFindSingleAttribute() {

    Query<Customer> query = Ebean.find(Customer.class);
    query.select("cast(concat('2000-',month(anniversary),'-', day(anniversary)) as date)::MonthDay as birthday")
    .where().startsWith("name", "RawSelect")
    .order("birthday");

    List<MonthDay> list = query.findSingleAttributeList();


    String sql = sqlOf(query, 5);
    //assertThat(sql).contains("select cast(concat('2000-',month(t0.anniversary),'-', day(t0.anniversary)) as date) birthday from o_customer t0 order by birthday");


    assertThat(list.get(0)).isEqualTo(MonthDay.of(7,4));
    assertThat(list.get(1)).isEqualTo(MonthDay.of(8,3));
    assertThat(list.get(2)).isEqualTo(MonthDay.of(11,22));
  }

  @Test
  public void testFindListInherit() {

    Query<Dog> query = Ebean.find(Dog.class);
    query.select("registrationNumber, cast(concat('2000-',month(dateOfBirth),'-', day(dateOfBirth)) as date)::MonthDay as c1@otherProps.birthday")
    .where().startsWith("registrationNumber", "RawSelect")
    .order("c1");

    List<Dog> list = query.findList();


    String sql = sqlOf(query, 5);
    //assertThat(sql).contains("select t0.species, t0.id, t0.registration_number, cast(concat('2000-',month(t0.date_of_birth),'-', day(t0.date_of_birth)) as date) birthday from animal t0 where t0.species in ('DOG','BDG')  order by birthday");


    assertThat(list.get(0).getOtherProps().get("birthday")).isEqualTo(MonthDay.of(7,4));
    assertThat(list.get(1).getOtherProps().get("birthday")).isEqualTo(MonthDay.of(8,3));
    assertThat(list.get(2).getOtherProps().get("birthday")).isEqualTo(MonthDay.of(11,22));

//    for (TEventOne eventOne : list) {
//      // lazy loading on Aggregation properties
//      // is not expected to work at this stage
//      Double totalAmount = eventOne.getTotalAmount();
//      assertThat(totalAmount).isNull();
//    }
  }

  @Test
  public void testFindList() {

    Query<Customer> query = Ebean.find(Customer.class);
    query.select("name, cast(concat('2000-',month(anniversary),'-', day(anniversary)) as date)::MonthDay as c1@otherProps.birthday")
    .where().startsWith("name", "RawSelect")
    .order("c1");

    List<Customer> list = query.findList();


    String sql = sqlOf(query, 5);
    //assertThat(sql).contains("select t0.id, t0.name, cast(concat('2000-',month(t0.anniversary),'-', day(t0.anniversary)) as date) birthday from o_customer t0 order by birthday");


    assertThat(list.get(0).getOtherProps().get("birthday")).isEqualTo(MonthDay.of(7,4));
    assertThat(list.get(1).getOtherProps().get("birthday")).isEqualTo(MonthDay.of(8,3));
    assertThat(list.get(2).getOtherProps().get("birthday")).isEqualTo(MonthDay.of(11,22));

//    for (TEventOne eventOne : list) {
//      // lazy loading on Aggregation properties
//      // is not expected to work at this stage
//      Double totalAmount = eventOne.getTotalAmount();
//      assertThat(totalAmount).isNull();
//    }
  }

}
