package org.querytest;

import io.ebean.DB;
import org.example.domain.ProductWithGenericLong;
import org.example.domain.ProductWithGenericMiddle;
import org.example.domain.ProductWithGenericString;
import org.example.domain.query.QProductWithGenericLong;
import org.example.domain.query.QProductWithGenericString;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QProductWithGenericTest {

  @Test
  void findByLongId() {

    var entity = new ProductWithGenericLong();
    entity.setId(42L);
    entity.setName("Gadget");
    entity.save();

    var result = new QProductWithGenericLong()
      .id.eq(42L)
      .findOne();

    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("Gadget");
  }

  @Test
  void findByStringId() {

    var entity = new ProductWithGenericString();
    entity.setId("1234");
    entity.setName("Gadget");
    entity.save();

    var result = new QProductWithGenericString()
      .id.eq("1234")
      .findOne();

    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("Gadget");
  }

  /**
   * Regression test for the NPE that occurred when DeployCreateProperties processed an entity
   * whose @Id type came from a TypeVariable two levels up in the generic superclass chain.
   *
   * <p>ProductWithGenericMiddle extends GenericMiddleModel&lt;Long&gt; extends GenericBaseModel&lt;Long&gt;
   * — the id field is declared as {@code T id} in GenericBaseModel, which requires composing the
   * generic type mappings across both superclass levels to resolve T → Long.
   */
  @Test
  void findById_multiLevelGenericSuperclass() {

    var entity = new ProductWithGenericMiddle();
    entity.setId(99L);
    entity.setName("Widget");
    entity.save();

    var result = DB.find(ProductWithGenericMiddle.class, 99L);

    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("Widget");
  }
}
