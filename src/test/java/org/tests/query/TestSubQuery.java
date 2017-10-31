package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.CKeyParent;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.basic.Vehicle;
import org.tests.model.basic.VehicleDriver;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class TestSubQuery extends BaseTestCase {

  @Test
  public void testId() {

    ResetBasicData.reset();

    List<Integer> productIds = new ArrayList<>();
    productIds.add(3);
    productIds.add(4);
    productIds.add(5);

    Query<Order> sq = Ebean.createQuery(Order.class).select("id").where()
      .in("details.product.id", productIds).query();

    Ebean.find(Order.class).where().in("id", sq).findList();
  }

  @Test
  public void test_IsIn() {

    ResetBasicData.reset();

    List<Integer> productIds = new ArrayList<>();
    productIds.add(3);

    Query<Order> sq = Ebean.createQuery(Order.class).select("id").where()
      .isIn("details.product.id", productIds).query();

    Ebean.find(Order.class).where().isIn("id", sq).findList();
  }

  public void testCompositeKey() {
    ResetBasicData.reset();

    Query<CKeyParent> sq = Ebean.createQuery(CKeyParent.class).select("id.oneKey")
      .setAutoTune(false).where().query();

    Query<CKeyParent> pq = Ebean.find(CKeyParent.class).where().in("id.oneKey", sq).query();

    pq.findList();

    String sql = pq.getGeneratedSql();

    String golden = "(t0.one_key) in (select t0.one_key from ckey_parent t0) ";

    assertThat(sql).contains(golden);
  }

  /**
   * show that ebean is not using the correct table name in the subquery (sq)
   *
   * public void testInheritance1() { ResetBasicData.reset();
   *
   * Query<Vehicle> sq = Ebean.createQuery(Vehicle.class) .select("id")
   * .setAutoTune(false) .where() .query();
   *
   * Query<VehicleDriver> pq = Ebean.find(VehicleDriver.class)
   * .where().in("vehicle.id", sq) .query();
   *
   * pq.findList();
   *
   * String sql = pq.getGeneratedSql(); System.err.println(sql);
   *
   * String golden = "(t0.vehicle_id) in (select t0.id from t0.vehicle t0)"; if
   * (sql.indexOf(golden) < 0) { System.out.println("failed sql:"+sql);
   * fail("golden string not found"); }
   *
   * }
   */

  /**
   * show that ebean is adding the discriminator to the list of columns in the
   * subquery
   */
  public void testInheritance2() {
    ResetBasicData.reset();

    Query<VehicleDriver> sq = Ebean.createQuery(VehicleDriver.class).select("vehicle")
      .setAutoTune(false).where().query();

    Query<Vehicle> pq = Ebean.find(Vehicle.class).where().in("id", sq).query();

    pq.findList();

    String sql = pq.getGeneratedSql();

    // TODO: If, after bugfixing, the system still join against vehicle I do not
    // know now, in our case, it is not necessary if not
    // using it in the where clause
    String golden = "(t0.id) in (select t0.vehicle_id from vehicle_driver t0 left join vehicle t1 on t1.id = t0.vehicle_id )";
    assertThat(sql).contains(golden);

  }

  /**
   * show that ebean is adding the discriminator to the list of columns in the
   * subquery. Second test to make sure that joining is still possible after
   * bugfixing testInheritance2.
   */
  public void testInheritance3() {
    ResetBasicData.reset();

    Query<VehicleDriver> sq = Ebean.createQuery(VehicleDriver.class).select("vehicle")
      .setAutoTune(false).where().eq("vehicle.licenseNumber", "abc").query();

    Query<Vehicle> pq = Ebean.find(Vehicle.class).where().in("id", sq).query();

    pq.findList();

    String sql = pq.getGeneratedSql();
    String golden = "(t0.id) in (select t0.vehicle_id from vehicle_driver t0 left join vehicle t1 on t1.id = t0.vehicle_id  where t1.license_number = ? )";
    assertThat(sql).contains(golden);
  }

  /**
   * show that ebean is using the wrong column (from the vehicle_driver table
   * instead of vehicle) for the selected column in the subquery. In contrast to
   * testInheritance2+3 this test forces ebean to "drill down" to the key of the
   * relation.
   */
  public void testInheritance4() {
    ResetBasicData.reset();

    Query<VehicleDriver> sq = Ebean.createQuery(VehicleDriver.class).select("vehicle.id")
      .setAutoTune(false).where().query();

    Query<Vehicle> pq = Ebean.find(Vehicle.class).where().in("id", sq).query();

    pq.findList();

    String sql = pq.getGeneratedSql();

    String golden = "(t0.id) in (select t0.vehicle_id from vehicle_driver t0 left join vehicle t1 on t1.id = t0.vehicle_id )";
    assertThat(sql).contains(golden);
  }
}
