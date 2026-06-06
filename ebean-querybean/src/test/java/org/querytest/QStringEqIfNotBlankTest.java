package org.querytest;

import io.ebean.DB;
import io.ebean.Database;
import org.example.domain.Customer;
import org.example.domain.query.QCustomer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.domain.query.QCustomer.Alias.name;

class QStringEqIfNotBlankTest {

  @Test
  void present_addsEqPredicate() {
    var q = new QCustomer()
      .select(name)
      .name.eqIfNotBlank("rob")
      .query();

    q.findList();
    assertThat(q.getGeneratedSql()).contains("where t0.name = ?");
  }

  @Test
  void null_skipsPredicate() {
    var q = new QCustomer()
      .select(name)
      .name.isNotNull()
      .name.eqIfNotBlank(null)
      .query();

    q.findList();
    assertThat(q.getGeneratedSql()).doesNotContain("t0.name = ?");
  }

  @Test
  void blankWhitespace_skipsPredicate() {
    var q = new QCustomer()
      .select(name)
      .name.isNotNull()
      .name.eqIfNotBlank("   ")
      .query();

    q.findList();
    assertThat(q.getGeneratedSql()).doesNotContain("t0.name = ?");
  }

  @Test
  void empty_skipsPredicate() {
    var q = new QCustomer()
      .select(name)
      .name.isNotNull()
      .name.eqIfNotBlank("")
      .query();

    q.findList();
    assertThat(q.getGeneratedSql()).doesNotContain("t0.name = ?");
  }

  @Test
  void present_trimsBoundValue() {
    Database db = DB.getDefault();
    String unique = "eqIfNotBlank-" + System.nanoTime();

    Customer customer = new Customer();
    customer.name = unique;
    db.save(customer);

    Customer found = new QCustomer(db)
      .name.eqIfNotBlank("  " + unique + "  ")
      .findOne();

    assertThat(found).isNotNull();
    assertThat(found.name).isEqualTo(unique);

    db.delete(customer);
  }
}
