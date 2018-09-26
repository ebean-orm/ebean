package io.ebeaninternal.server.deploy;

import io.ebean.BaseTestCase;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import org.junit.Test;
import org.tests.model.basic.Address;
import org.tests.model.basic.BWithQIdent;
import org.tests.model.basic.Customer;

import static org.assertj.core.api.Assertions.assertThat;


public class DeployPropertyParserTest extends BaseTestCase {

  private final BeanDescriptor<Customer> descriptor = getBeanDescriptor(Customer.class);

  private final BeanDescriptor<Address> addressBeanDescriptor = getBeanDescriptor(Address.class);

  private final BeanDescriptor<BWithQIdent> bWithQIdentDescriptor = getBeanDescriptor(BWithQIdent.class);

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
  public void withExplicitQuote_all_platforms() {
    assertThat(withQuoteParser().parse("t0.`CODE` like ?")).isEqualTo("t0.`CODE` like ?");
    assertThat(withQuoteParser().parse("t0.[CODE] like ?")).isEqualTo("t0.[CODE] like ?");
    assertThat(withQuoteParser().parse("t0.\"CODE\" like ?")).isEqualTo("t0.\"CODE\" like ?");
  }

  @Test
  @ForPlatform(value = {Platform.H2, Platform.POSTGRES})
  public void withQuote_when_match_h2() {
    assertThat(withQuoteParser().parse("name like ?")).isEqualTo("${}\"Name\" like ?");
  }

  @Test
  @ForPlatform(value = Platform.SQLSERVER)
  public void withQuote_when_match_sqlserver() {
    assertThat(withQuoteParser().parse("name like ?")).isEqualTo("${}[Name] like ?");
  }

  @Test
  @ForPlatform(value = Platform.MYSQL)
  public void withQuote_when_match_mysql() {
    assertThat(withQuoteParser().parse("name like ?")).isEqualTo("${}`Name` like ?");
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

  private DeployPropertyParser withQuoteParser() {
    return bWithQIdentDescriptor.parser();
  }

}
