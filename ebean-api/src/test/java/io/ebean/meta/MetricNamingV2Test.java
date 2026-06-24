package io.ebean.meta;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetricNamingV2Test {

  private MetricNamingV2.Mapped map(String name, String beanType) {
    return MetricNamingV2.map(name, beanType);
  }

  @Test
  void orm_withBeanType() {
    MetricNamingV2.Mapped m = map("orm.Customer.findList", "Customer");
    assertThat(m.name()).isEqualTo("ebean.query");
    assertThat(m.tags()).isEqualTo("kind:orm,label:Customer.findList,type:Customer");
  }

  @Test
  void orm_withoutBeanType() {
    MetricNamingV2.Mapped m = map("orm.Customer.findList", null);
    assertThat(m.name()).isEqualTo("ebean.query");
    assertThat(m.tags()).isEqualTo("kind:orm,label:Customer.findList");
  }

  @Test
  void dto_andSql() {
    assertThat(map("dto.CustomerDto.findRecent", "CustomerDto").tags())
      .isEqualTo("kind:dto,label:CustomerDto.findRecent,type:CustomerDto");
    assertThat(map("sql.query.fooBar", "Customer").tags())
      .isEqualTo("kind:sql,label:query.fooBar,type:Customer");
  }

  @Test
  void iud() {
    MetricNamingV2.Mapped m = map("iud.User.save", null);
    assertThat(m.name()).isEqualTo("ebean.dml");
    assertThat(m.tags()).isEqualTo("label:User.save");
  }

  @Test
  void txn_named_and_plain() {
    assertThat(map("txn.named.ProcessJob", null).name()).isEqualTo("ebean.txn");
    assertThat(map("txn.named.ProcessJob", null).tags()).isEqualTo("label:ProcessJob");
    assertThat(map("txn.main", null).tags()).isEqualTo("label:main");
  }

  @Test
  void l2_regionAndOp() {
    MetricNamingV2.Mapped m = map("l2.customer.hit", null);
    assertThat(m.name()).isEqualTo("ebean.l2");
    assertThat(m.tags()).isEqualTo("op:hit,region:customer");
  }

  @Test
  void l2_opOnly() {
    assertThat(map("l2.hit", null).tags()).isEqualTo("op:hit");
  }

  @Test
  void unrecognisedPrefix_isOther() {
    MetricNamingV2.Mapped m = map("l2n.Customer.hit", null);
    assertThat(m.name()).isEqualTo("ebean.other");
    assertThat(m.tags()).isEqualTo("label:l2n.Customer.hit");
  }

  @Test
  void noDot_isOther() {
    assertThat(map("jvm", null).name()).isEqualTo("ebean.other");
    assertThat(map("jvm", null).tags()).isEqualTo("label:jvm");
  }

  @Test
  void nullOrEmpty() {
    assertThat(map(null, null).name()).isEqualTo("ebean.other");
    assertThat(map(null, null).tags()).isEmpty();
    assertThat(map("", null).tags()).isEmpty();
  }

  @Test
  void sanitisesReservedChars() {
    MetricNamingV2.Mapped m = map("orm.Customer.weird", "Cust:om,er");
    assertThat(m.tags()).isEqualTo("kind:orm,label:Customer.weird,type:Cust_om_er");
  }

  @Test
  void tagsAreSortedByKey() {
    // kind < label < type alphabetically regardless of build order
    assertThat(map("orm.X.find", "Bean").tags())
      .isEqualTo("kind:orm,label:X.find,type:Bean");
  }
}
