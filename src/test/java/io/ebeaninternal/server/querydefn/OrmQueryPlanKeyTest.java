package io.ebeaninternal.server.querydefn;


import io.ebean.OrderBy;
import io.ebean.Query;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionList;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.TableJoin;
import io.ebeaninternal.server.deploy.meta.DeployTableJoin;
import io.ebeaninternal.server.deploy.meta.DeployTableJoinColumn;
import io.ebeaninternal.server.expression.BaseExpressionTest;
import org.tests.model.basic.Customer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OrmQueryPlanKeyTest extends BaseExpressionTest {


  @Test
  public void equals_when_defaults() {

    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);

    assertSame(key1, key2);
  }

  @Test
  public void equals_when_diffDiscValue() {

    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey("A", null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key3 = new OrmQueryPlanKey("B", null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    assertDifferent(key1, key2);
    assertDifferent(key2, key3);
  }

  @Test
  public void equals_when_diffTableJoinNull() {

    TableJoin tableJoin = tableJoin("table", "id", "customer_id");

    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, tableJoin, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);

    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_diffTableJoin() {

    TableJoin tableJoin1 = tableJoin("one", "id", "customer_id");
    TableJoin tableJoin2 = tableJoin("two", "id", "customer_id");

    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, tableJoin1, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, tableJoin2, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);

    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_sameTableJoin() {

    TableJoin tableJoin1 = tableJoin("one", "id", "customer_id");
    TableJoin tableJoin2 = tableJoin("one", "id", "customer_id");

    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, tableJoin1, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, tableJoin2, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);

    assertSame(key1, key2);
  }

  private TableJoin tableJoin(String table, String col1, String col2) {
    DeployTableJoin deploy = new DeployTableJoin();
    deploy.setTable(table);
    deploy.addJoinColumn(new DeployTableJoinColumn(col1, col2));
    return new TableJoin(deploy);
  }

  @Test
  public void equals_when_diffQueryType() {

    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.LIST, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);

    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_firstRowsDifferent() {

    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 10, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);

    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_maxRowsDifferent() {

    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 10, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);

    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_firstRowsMaxRowsSame() {

    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 10, 20, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 10, 20, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);

    assertSame(key1, key2);
  }

  @Test
  public void equals_when_diffDisableLazyLoading() {

    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, true, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);

    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_diffOrderByNull() {

    OrderBy<Object> o1 = new OrderBy<>("id");
    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, o1, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);

    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_orderBySame() {

    OrderBy<Object> o1 = new OrderBy<>("id, name");
    OrderBy<Object> o2 = new OrderBy<>("id, name");
    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, o1, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, o2, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);

    assertSame(key1, key2);
  }

  @Test
  public void equals_when_diffDistinct() {
    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, true, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_sameDistinct() {
    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, true, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, true, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    assertSame(key1, key2);
  }

  @Test
  public void equals_when_diffSqlDistinct() {
    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, true, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_sameSqlDistinct() {
    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, true, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, true, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    assertSame(key1, key2);
  }

  @Test
  public void equals_when_diffMapKeyNull() {
    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, "mapKey", null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_diffMapKey() {
    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, "mapKey", null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, "diff", null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_sameMapKey() {
    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, "mapKey", null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, "mapKey", null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    assertSame(key1, key2);
  }

  @Test
  public void equals_when_diffIdNull() {
    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, 42, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_idBothGiven() {
    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, 42, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, 23, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    assertSame(key1, key2);
  }

  @Test
  public void equals_when_diffTemporalMode() {
    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.DRAFT, null, null, null, null);
    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_diffForUpdate() {
    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, Query.ForUpdate.BASE, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_diffForUpdate_NoWait() {
    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, Query.ForUpdate.BASE, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, Query.ForUpdate.NOWAIT, null, null, null);
    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_diffForUpdate_SkipLocked() {
    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, Query.ForUpdate.BASE, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, Query.ForUpdate.SKIPLOCKED, null, null, null);
    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_diffRootAliasNull() {
    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, "rootAlias", null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_diffRootAlias() {
    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, "rootAlias", null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, "diff", null, null);
    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_sameRootAlias() {
    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, "rootAlias", null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, "rootAlias", null, null);
    assertSame(key1, key2);
  }

  SpiExpressionList<Customer> list_id_eq_42() {
    return (SpiExpressionList<Customer>) server().find(Customer.class)
      .where().eq("id", 42);
  }

  SpiExpressionList<Customer> list_id_eq_43() {
    return (SpiExpressionList<Customer>) server().find(Customer.class)
      .where().eq("id", 43);
  }

  SpiExpressionList<Customer> list_id_eq_42_and_name_eq_rob() {
    return (SpiExpressionList<Customer>) server().find(Customer.class)
      .where().eq("id", 43).eq("name", "rob");
  }


  @Test
  public void equals_when_sameWhere() {


    SpiExpressionList<Customer> list1 = list_id_eq_42();
    SpiExpressionList<Customer> list2 = list_id_eq_43();

    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, list1, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, list2, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    assertSame(key1, key2);
  }


  @Test
  public void equals_when_diffWhere() {

    SpiExpressionList<Customer> where1 = list_id_eq_42();
    SpiExpressionList<Customer> where2 = list_id_eq_42_and_name_eq_rob();

    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, where1, null, SpiQuery.TemporalMode.DRAFT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, where2, null, SpiQuery.TemporalMode.DRAFT, null, null, null, null);
    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_diffWhereNullLast() {

    SpiExpressionList<Customer> list1 = list_id_eq_42();

    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, list1, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_diffWhereNullFirst() {

    SpiExpressionList<Customer> list1 = list_id_eq_42();

    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, list1, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_diffHaving() {

    SpiExpression having1 = list_id_eq_42().copyForPlanKey();
    SpiExpression having2 = list_id_eq_42_and_name_eq_rob().copyForPlanKey();

    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, having1, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, having2, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_sameHaving() {

    SpiExpression having1 = list_id_eq_42().copyForPlanKey();
    SpiExpression having2 = list_id_eq_42().copyForPlanKey();

    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, having1, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, having2, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    assertSame(key1, key2);
  }

  @Test
  public void equals_when_havingNullLast() {

    SpiExpression having1 = list_id_eq_42().copyForPlanKey();

    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, having1, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    assertDifferent(key1, key2);
  }

  @Test
  public void equals_when_havingNullFirst() {

    SpiExpression having1 = list_id_eq_42().copyForPlanKey();

    OrmQueryPlanKey key1 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, null, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    OrmQueryPlanKey key2 = new OrmQueryPlanKey(null, null, SpiQuery.Type.BEAN, null, 0, 0, false, null, false, false, null, null, null, null, having1, SpiQuery.TemporalMode.CURRENT, null, null, null, null);
    assertDifferent(key1, key2);
  }

  private void assertDifferent(OrmQueryPlanKey key1, OrmQueryPlanKey key2) {
    assertThat(key1).isNotEqualTo(key2);
    assertThat(key1.hashCode()).isNotEqualTo(key2.hashCode());
  }

  private void assertSame(OrmQueryPlanKey key1, OrmQueryPlanKey key2) {
    assertThat(key1).isEqualTo(key2);
    assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
  }
}
