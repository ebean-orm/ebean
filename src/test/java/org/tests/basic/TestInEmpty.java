package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.Order;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.junit.Assert.assertEquals;

public class TestInEmpty extends BaseTestCase {

  @Test
  public void test_in_empty() {

    Query<Order> query = Ebean.find(Order.class).where().in("id", new Object[0]).gt("id", 0)
      .query();

    List<Order> list = query.findList();
    assertThat(query.getGeneratedSql()).contains("1=0");
    assertEquals(0, list.size());
  }

  @Test
  public void test_isIn_empty() {

    Query<Order> query = Ebean.find(Order.class).where().isIn("id", new Object[0]).gt("id", 0)
      .query();

    List<Order> list = query.findList();
    assertThat(query.getGeneratedSql()).contains("1=0");
    assertEquals(0, list.size());
  }
  
  @Test
  public void test_isIn_cache_key() {
    Set<Integer> set = new HashSet<>();
    // 1. Test with empty Set
    Ebean.find(Order.class).where().isIn("id", set).findEach(x -> System.out.println(x));
    
    // 2. Test with more than 100 in parameter
    for (int i = 0; i < 101; i++) {
      set.add(i);
    }
    Ebean.find(Order.class).where().isIn("id", set).findEach(x -> System.out.println(x));
    
    // 3. Test again with empty set
    set.clear();
    Ebean.find(Order.class).where().isIn("id", set).findEach(x -> System.out.println(x));
    
    // 4. Test with few parameters
    for (int i = 0; i < 5; i++) {
      set.add(i);
    }
    Ebean.find(Order.class).where().isIn("id", set).findEach(x -> System.out.println(x));
    
    // 5. Test again with empty set
    set.clear();
    Ebean.find(Order.class).where().isIn("id", set).findEach(x -> System.out.println(x));
  }
  
  @Test
  public void test_isIdIn_cache_key() {
    Set<Integer> set = new HashSet<>();
    // 1. Test with empty Set
    Ebean.find(Order.class).where().idIn(set).findEach(x -> System.out.println(x));
    
    // 2. Test with more than 100 in parameter
    for (int i = 0; i < 101; i++) {
      set.add(i);
    }
    Ebean.find(Order.class).where().idIn(set).findEach(x -> System.out.println(x));
    
    // 3. Test again with empty set
    set.clear();
    Ebean.find(Order.class).where().idIn(set).findEach(x -> System.out.println(x));
    
    // 4. Test with few parameters
    for (int i = 0; i < 5; i++) {
      set.add(i);
    }
    Ebean.find(Order.class).where().idIn(set).findEach(x -> System.out.println(x));
    
    // 5. Test again with empty set
    set.clear();
    Ebean.find(Order.class).where().idIn(set).findEach(x -> System.out.println(x));
  }

  @Test
  public void test_notIn_empty() {

    Query<Order> query = Ebean.find(Order.class).where().notIn("id", new Object[0]).gt("id", 0)
      .query();

    query.findList();
    assertThat(query.getGeneratedSql()).contains("1=1");
  }

}
