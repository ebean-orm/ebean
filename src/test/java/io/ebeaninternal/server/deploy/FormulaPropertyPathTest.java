package io.ebeaninternal.server.deploy;

import io.ebean.BaseTestCase;
import io.ebeaninternal.server.query.STreeProperty;
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

  @Test
  public void castFormula() {

    assertFormula("concat(name,'-end')::String", "concat", "name,'-end'", "String", null);
  }

  @Test
  public void cast_javaInstant() {

    assertFormula("max(updtime)::Instant", "max", "updtime", "Instant", null);
  }

  @Test
  public void alias() {
    assertFormula("concat(name,'-end') name", "concat", "name,'-end'", null, "name");
    assertFormula("concat(name,'-end') as name", "concat", "name,'-end'", null, "name");
  }

  @Test
  public void castAndAlias() {
    assertFormula("concat(name,'-end')::String name", "concat", "name,'-end'", "String", "name");
    assertFormula("concat(name,'-end')::String as name", "concat", "name,'-end'", "String", "name");
  }

  private void assertFormula(String input, String funcName, String expression) {
    assertFormula(input, funcName, expression, null, null);
  }

  private void assertFormula(String input, String funcName, String expression, String cast, String alias) {

    FormulaPropertyPath propertyPath = new FormulaPropertyPath(customerDesc, input);

    assertThat(propertyPath.internalExpression()).isEqualTo(expression);
    assertThat(propertyPath.outerFunction()).isEqualTo(funcName);
    if (cast != null) {
      assertThat(propertyPath.cast()).isEqualTo(cast);
    } else {
      assertThat(propertyPath.cast()).isNull();
    }
    if (alias != null) {
      assertThat(propertyPath.alias()).isEqualTo(alias);
    } else {
      assertThat(propertyPath.alias()).isNull();
    }

    STreeProperty treeProperty = propertyPath.build();

    assertThat(treeProperty).isNotNull();
  }
}
