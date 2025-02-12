package io.ebeaninternal.server.querydefn;


import io.ebean.ExpressionList;
import io.ebean.Query;
import io.ebeaninternal.api.CQueryPlanKey;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.BaseTest;
import io.ebeaninternal.server.deploy.TableJoin;
import io.ebeaninternal.server.deploy.meta.DeployTableJoin;
import io.ebeaninternal.server.deploy.meta.DeployTableJoinColumn;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

public class OrmQueryPlanKeyTest extends BaseTest {

  @SuppressWarnings({"unchecked", "rawtypes"})
  private DefaultOrmQuery<Customer> query() {
    return (DefaultOrmQuery) server().find(Customer.class);
  }

  @Test
  public void equals_when_defaults() {

    assertSame(query().createQueryPlanKey(), query().createQueryPlanKey());
  }

  @Test
  public void equals_when_hintIsDifferent_expect_different() {
    DefaultOrmQuery<Customer> q1 = query();
    q1.setHint("a");
    assertDifferent(q1, query());

    DefaultOrmQuery<Customer> q2 = query();
    q2.setHint("b");
    assertDifferent(q1, q2);
  }

  @Test
  public void equals_when_hintIsSame() {
    DefaultOrmQuery<Customer> q1 = query();
    q1.setHint("b");
    DefaultOrmQuery<Customer> q2 = query();
    q2.setHint("b");

    assertSame(q1, q2);
  }

  @Test
  public void equals_when_diffTableJoinNull() {

    DefaultOrmQuery<Customer> q1 = query();
    q1.setM2MIncludeJoin(tableJoin("table", "id", "customer_id"));

    assertDifferent(q1, query());
  }

  @Test
  public void equals_when_diffTableJoin() {

    DefaultOrmQuery<Customer> q1 = query();
    q1.setM2MIncludeJoin(tableJoin("one", "cid", "customer_id"));

    DefaultOrmQuery<Customer> q2 = query();
    q2.setM2MIncludeJoin(tableJoin("two", "cid", "customer_id"));

    assertDifferent(q1, q2);
  }

  @Test
  public void equals_when_sameTableJoin() {

    DefaultOrmQuery<Customer> q1 = query();
    q1.setM2MIncludeJoin(tableJoin("one", "id", "cust_id"));

    DefaultOrmQuery<Customer> q2 = query();
    q2.setM2MIncludeJoin(tableJoin("one", "id", "cust_id"));

    assertSame(q1, q2);
  }

  private TableJoin tableJoin(String table, String col1, String col2) {
    DeployTableJoin deploy = new DeployTableJoin();
    deploy.setTable(table);
    deploy.addJoinColumn(new DeployTableJoinColumn(col1, col2));
    return new TableJoin(deploy);
  }

  @Test
  public void equals_when_diffQueryType() {

    DefaultOrmQuery<Customer> q1 = query();
    q1.setType(SpiQuery.Type.BEAN);

    DefaultOrmQuery<Customer> q2 = query();
    q2.setType(SpiQuery.Type.LIST);

    assertDifferent(q1, q2);
  }

  @Test
  public void equals_when_firstRowsDifferent() {

    CQueryPlanKey key1 = planKey(query().setFirstRow(10));
    CQueryPlanKey key2 = planKey(query());
    assertDifferent(key1, key2);

    key1 = planKey(query().setFirstRow(10));
    key2 = planKey(query().setFirstRow(9));
    assertDifferent(key1, key2);

    key1 = planKey(query());
    key2 = planKey(query().setFirstRow(9));
    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_maxRowsDifferent() {

    CQueryPlanKey key1 = planKey(query().setMaxRows(10));
    CQueryPlanKey key2 = planKey(query());
    assertDifferent(key1, key2);

    key1 = planKey(query().setMaxRows(10));
    key2 = planKey(query().setMaxRows(9));
    assertDifferent(key1, key2);

    key1 = planKey(query());
    key2 = planKey(query().setMaxRows(9));
    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_firstRowsMaxRowsSame() {

    CQueryPlanKey key1 = planKey(query().setMaxRows(10).setFirstRow(20));
    CQueryPlanKey key2 = planKey(query().setFirstRow(20).setMaxRows(10));
    assertSame(key1, key2);
  }

  @Test
  public void equals_when_diffDisableLazyLoading() {

    assertDifferent(query().setDisableLazyLoading(true), query());
  }

  @Test
  public void equals_when_diffOrderByNull() {

    CQueryPlanKey key1 = planKey(query().orderBy("id"));
    CQueryPlanKey key2 = planKey(query());
    assertDifferent(key1, key2);

    key1 = planKey(query().orderBy().asc("id"));
    key2 = planKey(query());
    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_orderBySame() {

    CQueryPlanKey key1 = planKey(query().orderBy("id, name"));
    CQueryPlanKey key2 = planKey(query().orderBy("id, name"));
    assertSame(key1, key2);
  }

  @Test
  public void equals_when_diffDistinct() {

    CQueryPlanKey key1 = planKey(query().setDistinct(true));
    CQueryPlanKey key2 = planKey(query());
    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_sameDistinct() {

    CQueryPlanKey key1 = planKey(query().setDistinct(true));
    CQueryPlanKey key2 = planKey(query().setDistinct(true));
    assertSame(key1, key2);
  }

  @Test
  public void equals_when_diffMapKey() {

    CQueryPlanKey key1 = planKey(query().setMapKey("name"));
    CQueryPlanKey key2 = planKey(query());
    assertDifferent(key1, key2);

    CQueryPlanKey key3 = planKey(query().setMapKey("email"));
    assertDifferent(key1, key3);

    CQueryPlanKey key4 = planKey(query().setMapKey("name"));
    assertSame(key1, key4);
  }

  @Test
  public void equals_when_diffIdNull() {

    CQueryPlanKey key1 = planKey(query().setId(42));
    CQueryPlanKey key2 = planKey(query());
    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_idBothGiven() {

    CQueryPlanKey key1 = planKey(query().setId(42));
    CQueryPlanKey key2 = planKey(query().setId(23));
    assertSame(key1, key2);
  }

  @Test
  public void equals_when_diffTemporalMode() {

    CQueryPlanKey key1 = planKey(query());

    CQueryPlanKey key3 = planKey(query().asOf(new Timestamp(System.currentTimeMillis())));
    assertDifferent(key1, key3);

    CQueryPlanKey key4 = planKey(query().setIncludeSoftDeletes());
    assertDifferent(key1, key4);
  }

  @Test
  public void equals_when_diffForUpdate() {

    CQueryPlanKey key1 = planKey(query().forUpdate());
    CQueryPlanKey key2 = planKey(query());
    assertDifferent(key1, key2);

    CQueryPlanKey key3 = planKey(query().forUpdateNoWait());
    assertDifferent(key1, key3);

    CQueryPlanKey key4 = planKey(query().forUpdateSkipLocked());
    assertDifferent(key1, key4);

    CQueryPlanKey key5 = planKey(query().forUpdate());
    assertSame(key1, key5);
  }

  @Test
  public void equals_when_diffRootAliasNull() {

    CQueryPlanKey key1 = planKey(query().alias("alias"));
    CQueryPlanKey key2 = planKey(query());
    assertDifferent(key1, key2);

    CQueryPlanKey key3 = planKey(query().alias("diff"));
    assertDifferent(key1, key3);

    CQueryPlanKey key4 = planKey(query().alias("alias"));
    assertSame(key1, key4);
  }

  private DefaultOrmQuery<Customer> list_id_eq_42() {
    return (DefaultOrmQuery<Customer>) server().find(Customer.class)
      .where().eq("id", 42).query();
  }

  private DefaultOrmQuery<Customer> list_id_eq_43() {
    return (DefaultOrmQuery<Customer>) server().find(Customer.class)
      .where().eq("id", 43).query();
  }

  private DefaultOrmQuery<Customer> list_id_eq_42_and_name_eq_rob() {
    return (DefaultOrmQuery<Customer>) server().find(Customer.class)
      .where().eq("id", 43).eq("name", "rob").query();
  }


  @Test
  public void equals_when_sameWhere() {

    assertSame(list_id_eq_42(), list_id_eq_43());
  }


  @Test
  public void equals_when_diffWhere() {

    assertDifferent(list_id_eq_42(), list_id_eq_42_and_name_eq_rob());
  }

  @Test
  public void equals_when_diffWhereNullLast() {

    assertDifferent(list_id_eq_42(), query());
  }

  @Test
  public void equals_when_diffHaving() {

    CQueryPlanKey key1 = planKey(query().having().eq("id", 42));
    CQueryPlanKey key2 = planKey(query().having().eq("id", 42).eq("name", "Rob"));
    assertDifferent(key1, key2);

    CQueryPlanKey key3 = planKey(query().where().eq("id", 42));
    assertDifferent(key1, key3);

    CQueryPlanKey key4 = planKey(query().having().eq("name", "Rob"));
    assertDifferent(key1, key4);
  }

  @Test
  public void equals_when_sameHaving() {

    CQueryPlanKey key1 = planKey(query().having().eq("id", 42));
    CQueryPlanKey key2 = planKey(query().having().eq("id", 43));
    assertSame(key1, key2);
  }

  @Test
  public void equals_when_manualId_andSelectClause() {
    DefaultOrmQuery<Customer> q1 = (DefaultOrmQuery<Customer>)query().select("name");
    q1.setManualId();

    assertDifferent(q1, query().select("name"));
  }

  @Test
  public void equals_when_manualId_andNoSelectClause() {
    DefaultOrmQuery<Customer> q1 = query();
    q1.setManualId();

    assertSame(q1, query());
  }

  private CQueryPlanKey planKey(ExpressionList<Customer> id) {
    return planKey(id.query());
  }

  @SuppressWarnings({"rawtypes"})
  private CQueryPlanKey planKey(Query<Customer> query) {
    return ((DefaultOrmQuery) query).createQueryPlanKey();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void assertDifferent(Query q1, Query q2) {
    assertDifferent(planKey(q1), planKey(q2));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void assertSame(Query q1, Query q2) {
    assertSame(planKey(q1), planKey(q2));
  }

  private void assertDifferent(CQueryPlanKey key1, CQueryPlanKey key2) {
    assertThat(key1).isNotEqualTo(key2);
    assertThat(key1.hashCode()).isNotEqualTo(key2.hashCode());
  }

  private void assertSame(CQueryPlanKey key1, CQueryPlanKey key2) {
    assertThat(key1).isEqualTo(key2);
    assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
  }
}
