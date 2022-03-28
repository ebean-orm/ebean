package io.ebeaninternal.server.deploy;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Address;
import org.tests.model.basic.BWithQIdent;
import org.tests.model.basic.Customer;

public class DeployPropertyParserTest extends BaseTest {

  private final BeanDescriptor<Customer> descriptor = getBeanDescriptor(Customer.class);

  private final BeanDescriptor<Address> addressBeanDescriptor = getBeanDescriptor(Address.class);

  private final BeanDescriptor<BWithQIdent> bWithQIdentDescriptor = getBeanDescriptor(BWithQIdent.class);

  @Test
  public void from_prefix_expect_unchanged() {
    Assertions.assertThat(parser().parse("(select x from status join status)")).isEqualTo("(select x from status join status)");
  }

  @Test
  public void depth0_path() {
    Assertions.assertThat(parser().parse("pre status post")).isEqualTo("pre ${}status post");
  }

  @Test
  public void depth1_path() {
    Assertions.assertThat(parser().parse("billingAddress.city")).isEqualTo("${billingAddress}city");
  }

  @Test
  public void depth2_path() {
    Assertions.assertThat(parser().parse("max(billingAddress.country.name)")).isEqualTo("max(${billingAddress.country}name)");
  }

  @Test
  public void simpleMax() {
    Assertions.assertThat(parser().parse("max(name)")).isEqualTo("max(${}name)");
  }

  @Test
  public void combined() {
    Assertions.assertThat(parser().parse("sum(status * name)")).isEqualTo("sum(${}status * ${}name)");
  }

  @Test
  public void combined_withAtColumn() {
    Assertions.assertThat(addressParser().parse("concat(line1, line2, '-EA')")).isEqualTo("concat(${}line_1, ${}line_2, '-EA')");
  }

  @Test
  public void withExplicitQuote_all_platforms() {
    Assertions.assertThat(withQuoteParser().parse("t0.`CODE` like ?")).isEqualTo("t0.`CODE` like ?");
    Assertions.assertThat(withQuoteParser().parse("t0.[CODE] like ?")).isEqualTo("t0.[CODE] like ?");
    Assertions.assertThat(withQuoteParser().parse("t0.\"CODE\" like ?")).isEqualTo("t0.\"CODE\" like ?");
  }

  @Test
  public void withQuote_when_match_h2() {
    if (isH2() || isPostgres()) {
      Assertions.assertThat(withQuoteParser().parse("name like ?")).isEqualTo("${}\"Name\" like ?");
    } else if (isSqlServer()) {
      Assertions.assertThat(withQuoteParser().parse("name like ?")).isEqualTo("${}[Name] like ?");
    } else if (isMySql()) {
      Assertions.assertThat(withQuoteParser().parse("name like ?")).isEqualTo("${}`Name` like ?");
    }
  }

  @Test
  public void unknown_path() {
    Assertions.assertThat(parser().parse(" foo ")).isEqualTo(" foo ");
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
