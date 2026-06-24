package org.tests.query.aggregation;

import io.ebean.DB;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests aggregation queries over @Formula and @Formula2 properties.
 * <p>
 * AggStockProduct.availableQuantity — @Formula (simple, same-table expression)
 * AggStockProduct.totalPrice        — @Formula2 (logical path, auto-joins product)
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestAggregatedFormulaField extends BaseTestCase {

  private AggStockProduct stock1;
  private AggStockProduct stock2;

  @BeforeAll
  void setup() {
    AggProduct product1 = new AggProduct();
    product1.setName("product1");
    product1.setPrice(1);
    DB.save(product1);

    AggProduct product2 = new AggProduct();
    product2.setName("product2");
    product2.setPrice(2);
    DB.save(product2);

    stock1 = new AggStockProduct();
    stock1.setStock("s1");
    stock1.setQuantity(1);
    stock1.setProduct(product1);
    DB.save(stock1);
    DB.refresh(stock1);

    stock2 = new AggStockProduct();
    stock2.setStock("s1");
    stock2.setQuantity(2);
    stock2.setQuantityReserved(1);
    stock2.setProduct(product2);
    DB.save(stock2);
    DB.refresh(stock2);
  }

  @AfterAll
  void teardown() {
    DB.find(AggStockProduct.class).delete();
    DB.find(AggProduct.class).delete();
  }

  /**
   * Verify row-level @Formula value: availableQuantity = quantity - quantityReserved.
   */
  @Test
  void formula_availableQuantity_rowLevel() {
    assertThat(stock1.getAvailableQuantity()).isEqualTo(1);  // 1 - 0
    assertThat(stock2.getAvailableQuantity()).isEqualTo(1);  // 2 - 1
  }

  /**
   * Aggregate SUM over a @Formula property (no join).
   */
  @Test
  void formula_availableQuantity_aggregateSum() {
    int expected = stock1.getAvailableQuantity() + stock2.getAvailableQuantity();
    Number result = DB.find(AggStockProduct.class)
      .select("SUM(availableQuantity)")
      .where().eq("stock", "s1")
      .findSingleAttribute();
    assertThat(result.intValue()).isEqualTo(expected);
  }

  /**
   * Verify row-level @Formula2 value: totalPrice = quantity * product.price (auto-joins product).
   */
  @Test
  void formula2_totalPrice_rowLevel() {
    // stock1: quantity=1, product.price=1 → 1
    assertThat(stock1.getTotalPrice()).isEqualTo(1);
    // stock2: quantity=2, product.price=2 → 4
    assertThat(stock2.getTotalPrice()).isEqualTo(4);
  }

  /**
   * Verify the SQL generated for @Formula2 includes an auto-join to product.
   */
  @Test
  void formula2_totalPrice_generatesJoin() {
    LoggedSql.start();
    List<AggStockProduct> list = DB.find(AggStockProduct.class)
      .select("totalPrice")
      .where().eq("stock", "s1")
      .findList();
    List<String> sql = LoggedSql.stop();

    assertThat(list).hasSize(2);
    assertThat(sql).hasSize(1);
    // @Formula2 auto-joins product table
    assertThat(sql.get(0)).contains("left join agg_product");
  }

  /**
   * Aggregate SUM over a @Formula2 property (auto-joins product, no explicit fetch needed).
   */
  @Test
  void formula2_totalPrice_aggregateSum() {
    // stock1: 1*1=1, stock2: 2*2=4 → total = 5
    int expected = stock1.getTotalPrice() + stock2.getTotalPrice();

    LoggedSql.start();
    Number result = DB.find(AggStockProduct.class)
      .select("SUM(totalPrice)")
      .where().eq("stock", "s1")
      .findSingleAttribute();
    List<String> sql = LoggedSql.stop();

    assertThat(result.intValue()).isEqualTo(expected);
    // auto-join to product must be generated even without explicit fetch
    assertThat(sql.get(0)).contains("left join agg_product");
  }
}
