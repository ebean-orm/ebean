package io.ebeaninternal.server.deploy;

import io.ebean.BaseTestCase;
import org.junit.Test;
import org.tests.model.basic.Address;
import org.tests.model.basic.Customer;

import static org.assertj.core.api.Assertions.assertThat;


public class DeployPropertyParserTest extends BaseTestCase {

  private final BeanDescriptor<Customer> descriptor = getBeanDescriptor(Customer.class);

  private final BeanDescriptor<Address> addressBeanDescriptor = getBeanDescriptor(Address.class);

  @Test
  public void from_prefix_expect_unchanged() {
    assertThat(parser().parse("(select x from status join status)")).isEqualTo("(select x from status join status)");
  }

  @Test
  public void depth0_path() {
    assertThat(parser().parse("pre status post")).isEqualTo("pre ${}status post");
  }

  @Test
  public void depth1_path() {
    assertThat(parser().parse("billingAddress.city")).isEqualTo("${billingAddress}city");
  }

  @Test
  public void depth2_path() {
    assertThat(parser().parse("max(billingAddress.country.name)")).isEqualTo("max(${billingAddress.country}name)");
  }

  @Test
  public void simpleMax() {
    assertThat(parser().parse("max(name)")).isEqualTo("max(${}name)");
  }

  @Test
  public void combined() {
    assertThat(parser().parse("sum(status * name)")).isEqualTo("sum(${}status * ${}name)");
  }

  @Test
  public void combined_withAtColumn() {
    assertThat(addressParser().parse("concat(line1, line2, '-EA')")).isEqualTo("concat(${}line_1, ${}line_2, '-EA')");
  }

  @Test
  public void unknown_path() {
    assertThat(parser().parse(" foo ")).isEqualTo(" foo ");
  }

  private DeployPropertyParser parser() {
    return descriptor.parser();
  }

  private DeployPropertyParser addressParser() {
    return addressBeanDescriptor.parser();
  }

}
