package io.ebeaninternal.server.deploy;

import io.ebean.BaseTestCase;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FormulaPropertyPathTest extends BaseTestCase {

  //private BeanDescriptor<Customer> customerDesc = getBeanDescriptor(Customer.class);

  @Test
  public void isFormula() {

    assertFormula("max(foo)", "max", "foo");
    assertFormula("min(bar)", "min", "bar");
    assertFormula("avg(baz)", "avg", "baz");
  }

  @Test
  public void isFormula_count() {
    assertFormula("count(moo)", "count", "moo");
    assertFormula("count(distinct joo)", "count", "joo");
  }

  private void assertFormula(String input, String aggType, String baseProperty) {

    FormulaPropertyPath propertyPath = new FormulaPropertyPath(input);

    assertThat(propertyPath.isFormula()).isTrue();
    assertThat(propertyPath.basePropertyName()).isEqualTo(baseProperty);
    assertThat(propertyPath.aggType()).isEqualTo(aggType);

  }
}
