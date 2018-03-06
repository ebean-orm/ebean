package io.ebeaninternal.server.deploy;

import io.ebean.BaseTestCase;
import io.ebeaninternal.server.query.SqlTreeProperty;
import org.junit.Test;
import org.tests.model.basic.Customer;

import static org.assertj.core.api.Assertions.assertThat;

public class FormulaPropertyPathTest extends BaseTestCase {

  private BeanDescriptor<Customer> customerDesc = getBeanDescriptor(Customer.class);

  @Test
  public void isFormula() {

    assertFormula("max(version)", "max", "version");
    assertFormula("min(name)", "min", "name");
    assertFormula("avg(id)", "avg", "id");
  }

  @Test
  public void isFormula_count() {
    assertFormula("count(status)", "count", "status");
    assertFormula("count(distinct name)", "count", "name");
  }

  @Test
  public void concat() {

    assertFormula("concat(name,'-end')", "concat", "name,'-end'");
  }

  private void assertFormula(String input, String aggType, String baseProperty) {

    FormulaPropertyPath propertyPath = new FormulaPropertyPath(customerDesc, input);

    assertThat(propertyPath.basePropertyName()).isEqualTo(baseProperty);
    assertThat(propertyPath.aggType()).isEqualTo(aggType);

    SqlTreeProperty treeProperty = propertyPath.build();

    assertThat(treeProperty).isNotNull();
  }
}
