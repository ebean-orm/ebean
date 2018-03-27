package io.ebeaninternal.server.querydefn;


import io.ebean.ExpressionList;
import io.ebean.Query;
import io.ebeaninternal.api.CQueryPlanKey;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.TableJoin;
import io.ebeaninternal.server.deploy.meta.DeployTableJoin;
import io.ebeaninternal.server.deploy.meta.DeployTableJoinColumn;
import io.ebeaninternal.server.expression.BaseExpressionTest;
import org.junit.Test;
import org.tests.model.basic.Customer;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

public class OrmQueryPlanKeyTest extends BaseExpressionTest {


  @SuppressWarnings("unchecked")
  private DefaultOrmQuery<Customer> query() {
    return (DefaultOrmQuery) server().find(Customer.class);
  }

  @Test
  public void equals_when_defaults() {

    assertSame(query().createQueryPlanKey(), query().createQueryPlanKey());
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

    CQueryPlanKey key1 = query().setFirstRow(10).createQueryPlanKey();
    CQueryPlanKey key2 = query().createQueryPlanKey();
    assertDifferent(key1, key2);

    key1 = query().setFirstRow(10).createQueryPlanKey();
    key2 = query().setFirstRow(9).createQueryPlanKey();
    assertDifferent(key1, key2);

    key1 = query().createQueryPlanKey();
    key2 = query().setFirstRow(9).createQueryPlanKey();
    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_maxRowsDifferent() {

    CQueryPlanKey key1 = query().setMaxRows(10).createQueryPlanKey();
    CQueryPlanKey key2 = query().createQueryPlanKey();
    assertDifferent(key1, key2);

    key1 = query().setMaxRows(10).createQueryPlanKey();
    key2 = query().setMaxRows(9).createQueryPlanKey();
    assertDifferent(key1, key2);

    key1 = query().createQueryPlanKey();
    key2 = query().setMaxRows(9).createQueryPlanKey();
    assertDifferent(key1, key2);

  }

  @Test
  public void equals_when_firstRowsMaxRowsSame() {

    CQueryPlanKey key1 = query().setMaxRows(10).setFirstRow(20).createQueryPlanKey();
    CQueryPlanKey key2 = query().setFirstRow(20).setMaxRows(10).createQueryPlanKey();
    assertSame(key1, key2);
  }

  @Test
  public void equals_when_diffDisableLazyLoading() {

    assertDifferent(query().setDisableLazyLoading(true), query());
  }

  @Test
  public void equals_when_diffOrderByNull() {

    CQueryPlanKey key1 = query().order("id").createQueryPlanKey();
    CQueryPlanKey key2 = query().createQueryPlanKey();
    assertDifferent(key1, key2);

    key1 = ((DefaultOrmQuery) query().order().asc("id")).createQueryPlanKey();
    key2 = query().createQueryPlanKey();
    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_orderBySame() {

    CQueryPlanKey key1 = query().order("id, name").createQueryPlanKey();
    CQueryPlanKey key2 = query().order("id, name").createQueryPlanKey();
    assertSame(key1, key2);
  }

  @Test
  public void equals_when_diffDistinct() {

    CQueryPlanKey key1 = query().setDistinct(true).createQueryPlanKey();
    CQueryPlanKey key2 = query().createQueryPlanKey();
    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_sameDistinct() {

    CQueryPlanKey key1 = query().setDistinct(true).createQueryPlanKey();
    CQueryPlanKey key2 = query().setDistinct(true).createQueryPlanKey();
    assertSame(key1, key2);
  }

  @Test
  public void equals_when_diffSqlDistinct() {
    DefaultOrmQuery<Customer> q1 = query();
    q1.setSqlDistinct(true);
    CQueryPlanKey key1 = q1.createQueryPlanKey();
    CQueryPlanKey key2 = query().createQueryPlanKey();

    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_sameSqlDistinct() {

    DefaultOrmQuery<Customer> q1 = query();
    q1.setSqlDistinct(true);

    DefaultOrmQuery<Customer> q2 = query();
    q2.setSqlDistinct(true);

    assertSame(q1, q2);
  }

  @Test
  public void equals_when_useDocStore() {
    CQueryPlanKey key1 = query().setUseDocStore(true).createQueryPlanKey();
    CQueryPlanKey key2 = query().createQueryPlanKey();

    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_diffMapKey() {

    CQueryPlanKey key1 = query().setMapKey("name").createQueryPlanKey();
    CQueryPlanKey key2 = query().createQueryPlanKey();
    assertDifferent(key1, key2);

    CQueryPlanKey key3 = query().setMapKey("email").createQueryPlanKey();
    assertDifferent(key1, key3);

    CQueryPlanKey key4 = query().setMapKey("name").createQueryPlanKey();
    assertSame(key1, key4);
  }

  @Test
  public void equals_when_diffIdNull() {

    CQueryPlanKey key1 = query().setId(42).createQueryPlanKey();
    CQueryPlanKey key2 = query().createQueryPlanKey();
    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_idBothGiven() {

    CQueryPlanKey key1 = query().setId(42).createQueryPlanKey();
    CQueryPlanKey key2 = query().setId(23).createQueryPlanKey();
    assertSame(key1, key2);
  }

  @Test
  public void equals_when_diffTemporalMode() {

    CQueryPlanKey key1 = query().createQueryPlanKey();
    CQueryPlanKey key2 = query().asDraft().createQueryPlanKey();
    assertDifferent(key1, key2);

    CQueryPlanKey key3 = query().asOf(new Timestamp(System.currentTimeMillis())).createQueryPlanKey();
    assertDifferent(key1, key3);

    CQueryPlanKey key4 = query().setIncludeSoftDeletes().createQueryPlanKey();
    assertDifferent(key1, key4);
  }

  @Test
  public void equals_when_diffForUpdate() {

    CQueryPlanKey key1 = query().forUpdate().createQueryPlanKey();
    CQueryPlanKey key2 = query().createQueryPlanKey();
    assertDifferent(key1, key2);

    CQueryPlanKey key3 = query().forUpdateNoWait().createQueryPlanKey();
    assertDifferent(key1, key3);

    CQueryPlanKey key4 = query().forUpdateSkipLocked().createQueryPlanKey();
    assertDifferent(key1, key4);

    CQueryPlanKey key5 = query().forUpdate().createQueryPlanKey();
    assertSame(key1, key5);
  }

  @Test
  public void equals_when_diffRootAliasNull() {

    CQueryPlanKey key1 = query().alias("alias").createQueryPlanKey();
    CQueryPlanKey key2 = query().createQueryPlanKey();
    assertDifferent(key1, key2);

    CQueryPlanKey key3 = query().alias("diff").createQueryPlanKey();
    assertDifferent(key1, key3);

    CQueryPlanKey key4 = query().alias("alias").createQueryPlanKey();
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
  public void equals_when_manualId() {

    DefaultOrmQuery<Customer> q1 = query();
    q1.setManualId(true);

    assertDifferent(q1, query());
  }

  private CQueryPlanKey planKey(ExpressionList<Customer> id) {
    return planKey(id.query());
  }

  private CQueryPlanKey planKey(Query<Customer> query) {
    return ((DefaultOrmQuery) query).createQueryPlanKey();
  }

  private void assertDifferent(DefaultOrmQuery q1, DefaultOrmQuery q2) {
    assertDifferent(q1.createQueryPlanKey(), q2.createQueryPlanKey());
  }

  private void assertSame(DefaultOrmQuery q1, DefaultOrmQuery q2) {
    assertSame(q1.createQueryPlanKey(), q2.createQueryPlanKey());
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
