package io.ebean.text;

import io.ebean.FetchPath;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class PathPropertiesTests {


  @Test
  public void test_noParentheses() {

    PathProperties s0 = PathProperties.parse("id,name");

    assertEquals(1, s0.getPathProps().size());
    assertTrue(s0.getProperties(null).contains("id"));
    assertTrue(s0.getProperties(null).contains("name"));
    assertFalse(s0.getProperties(null).contains("status"));
  }

  @Test
  public void test_noParentheses_needTrim() {

    PathProperties s0 = PathProperties.parse(" id, name ");

    assertEquals(1, s0.getPathProps().size());
    assertTrue(s0.getProperties(null).contains("id"));
    assertTrue(s0.getProperties(null).contains("name"));
    assertFalse(s0.getProperties(null).contains("status"));
  }

  @Test
  public void test_withParentheses() {

    PathProperties s0 = PathProperties.parse("(id,name)");

    assertEquals(1, s0.getPathProps().size());
    assertTrue(s0.getProperties(null).contains("id"));
    assertTrue(s0.getProperties(null).contains("name"));
    assertFalse(s0.getProperties(null).contains("status"));
  }

  @Test
  public void test_withColon() {

    PathProperties s0 = PathProperties.parse(":(id,name)");

    assertEquals(1, s0.getPathProps().size());
    assertTrue(s0.getProperties(null).contains("id"));
    assertTrue(s0.getProperties(null).contains("name"));
    assertFalse(s0.getProperties(null).contains("status"));
  }

  @Test
  public void test_nested() {

    PathProperties s1 = PathProperties.parse("id,name,shipAddr(*)");
    assertEquals(2, s1.getPathProps().size());
    assertEquals(3, s1.getProperties(null).size());
    assertTrue(s1.getProperties(null).contains("id"));
    assertTrue(s1.getProperties(null).contains("name"));
    assertTrue(s1.getProperties(null).contains("shipAddr"));
    assertTrue(s1.getProperties("shipAddr").contains("*"));
    assertEquals(1, s1.getProperties("shipAddr").size());
  }

  @Test
  public void test_withParenthesesColonNested() {

    PathProperties s1 = PathProperties.parse(":(id,name,shipAddr(*))");
    assertEquals(2, s1.getPathProps().size());
    assertEquals(3, s1.getProperties(null).size());
    assertTrue(s1.getProperties(null).contains("id"));
    assertTrue(s1.getProperties(null).contains("name"));
    assertTrue(s1.getProperties(null).contains("shipAddr"));
    assertTrue(s1.getProperties("shipAddr").contains("*"));
    assertEquals(1, s1.getProperties("shipAddr").size());
  }

  @Test
  public void test_add() {

    PathProperties root = PathProperties.parse("status,date");
    root.addNested("customer", PathProperties.parse("id,name"));

    FetchPath expect = PathProperties.parse("status,date,customer(id,name)");
    assertThat(root.toString()).isEqualTo(expect.toString());
  }

  @Test
  public void test_add_nested() {

    PathProperties root = PathProperties.parse("status,date");
    root.addNested("customer", PathProperties.parse("id,name,address(line1,city)"));

    FetchPath expect = PathProperties.parse("status,date,customer(id,name,address(line1,city))");
    assertThat(root.toString()).isEqualTo(expect.toString());
  }

  @Test
  public void test_all_properties() {

    FetchPath root = PathProperties.parse("*");
    assertThat(root.getProperties(null)).containsExactly("*");
  }

  @Test
  public void test_all_properties_multipleLevels() {

    PathProperties root = PathProperties.parse("*,customer(*)");
    //PathProperties.Props rootProps = root.getProps(null);
    PathProperties.Props customerProps = root.getProps("customer");

    assertThat(root.getProperties(null)).containsExactly("*", "customer");
    assertThat(customerProps.getPropertiesAsString()).isEqualTo("*");
  }

  @Test
  public void test_includesProperty_when_wildcardUsed() {

    PathProperties root = PathProperties.parse("*,customer(*)");

    assertTrue(root.includesProperty("id"));
    assertTrue(root.includesProperty("name"));
    assertTrue(root.includesProperty("customer.id"));
    assertTrue(root.includesProperty("customer.name"));

    assertFalse(root.includesProperty("details.id"));
    assertTrue(root.includesProperty("details"));

    assertFalse(root.includesPath("details"));
  }

  @Test
  public void test_includesProperty_when_specificPropertiesUsed() {

    PathProperties root = PathProperties.parse("id,name,customer(*,billingAddress(city))");

    assertTrue(root.includesProperty("id"));
    assertTrue(root.includesProperty("name"));
    assertFalse(root.includesProperty("status"));

    assertTrue(root.includesProperty("customer.id"));
    assertTrue(root.includesProperty("customer.foo"));
    assertTrue(root.includesProperty("customer.billingAddress"));
    assertTrue(root.includesProperty("customer.billingAddress.city"));

    assertFalse(root.includesPath("customer.shippingAddress"));
    assertFalse(root.includesPath("customer", "shippingAddress"));
    assertFalse(root.includesProperty("customer.shippingAddress.city"));

    assertTrue(root.includesPath(null));
    assertTrue(root.includesPath("customer"));
    assertTrue(root.includesPath("customer.billingAddress"));
    assertTrue(root.includesPath("customer", "billingAddress"));

    assertFalse(root.includesPath("customer.shippingAddress"));
    assertFalse(root.includesPath("details"));
  }

  @Test
  public void test_includesPropertyWithPrefix() {

    PathProperties root = PathProperties.parse("id,name,customer(*,billingAddress(city))");

    assertTrue(root.includesProperty("customer", "id"));
    assertTrue(root.includesProperty("customer", "billingAddress"));
    assertTrue(root.includesProperty("customer.billingAddress", "city"));

    assertFalse(root.includesPath("customer", "shippingAddress"));
    assertFalse(root.includesProperty("customer.shippingAddress", "city"));
  }

  @Test
  public void test_includesPathWithPrefix() {

    PathProperties root = PathProperties.parse("id,name,customer(*,billingAddress(city))");

    assertTrue(root.includesPath(null, "customer"));
    assertTrue(root.includesPath("customer", "billingAddress"));

    assertFalse(root.includesPath(null, "details"));
    assertFalse(root.includesPath("customer", "shippingAddress"));
  }
}
