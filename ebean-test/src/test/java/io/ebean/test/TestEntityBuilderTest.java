package io.ebean.test;

import io.ebean.DB;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasic;
import org.tests.model.basic.UUOne;
import org.tests.model.basic.UUTwo;

import static org.assertj.core.api.Assertions.assertThat;

class TestEntityBuilderTest extends BaseTestCase {

  private final TestEntityBuilder builder = TestEntityBuilder.builder(DB.getDefault()).build();

  @Test
  void build_simpleEntity_populatesScalarFields() {
    EBasic bean = builder.build(EBasic.class);

    assertThat(bean).isNotNull();
    assertThat(bean.getId()).isNull();           // @Id — not populated
    assertThat(bean.getName()).isNotNull();      // String scalar — populated
    assertThat(bean.getName()).hasSizeLessThanOrEqualTo(127); // @Size(max=127) respected
    assertThat(bean.getDescription()).isNotNull();
    assertThat(bean.getStatus()).isNotNull();    // Enum — populated with first constant
    assertThat(bean.getStatus()).isEqualTo(EBasic.Status.NEW);
  }

  @Test
  void build_entityWithCascadeManyToOne_populatesRelationship() {
    UUTwo bean = builder.build(UUTwo.class);

    assertThat(bean).isNotNull();
    assertThat(bean.getId()).isNull();           // @Id — not populated
    assertThat(bean.getVersion()).isZero();      // @Version — not populated
    assertThat(bean.getName()).isNotNull();
    assertThat(bean.getMaster()).isNotNull();    // @ManyToOne(cascade=PERSIST) — recursively built
    assertThat(bean.getMaster().getName()).isNotNull();
    assertThat(bean.getMaster().getId()).isNull(); // @Id on UUOne — not populated
    assertThat(bean.getMaster().getVersion()).isZero(); // @Version on UUOne — not populated
  }

  @Test
  void build_calledTwice_producesDistinctInstances() {
    EBasic first = builder.build(EBasic.class);
    EBasic second = builder.build(EBasic.class);

    assertThat(first).isNotSameAs(second);
    // String values should be different random values
    assertThat(first.getName()).isNotEqualTo(second.getName());
  }

  @Test
  void save_insertsEntityAndReturnsWithId() {
    EBasic saved = builder.save(EBasic.class);

    assertThat(saved).isNotNull();
    assertThat(saved.getId()).isNotNull();   // @Id assigned after insert
    assertThat(saved.getName()).isNotNull();

    // verify it's actually in the database
    EBasic found = DB.find(EBasic.class, saved.getId());
    assertThat(found).isNotNull();
    assertThat(found.getName()).isEqualTo(saved.getName());
  }

  @Test
  void save_entityWithCascadeManyToOne_savesCascades() {
    UUTwo saved = builder.save(UUTwo.class);

    assertThat(saved).isNotNull();
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getMaster()).isNotNull();
    assertThat(saved.getMaster().getId()).isNotNull(); // parent also saved via cascade

    UUOne foundMaster = DB.find(UUOne.class, saved.getMaster().getId());
    assertThat(foundMaster).isNotNull();
  }

  @Test
  void saveAll_insertsMultipleBeans_andAllCanBeFound() {
    EBasic bean1 = builder.build(EBasic.class);
    EBasic bean2 = builder.build(EBasic.class);
    EBasic bean3 = builder.build(EBasic.class);

    builder.saveAll(bean1, bean2, bean3);

    assertThat(bean1.getId()).isNotNull();
    assertThat(bean2.getId()).isNotNull();
    assertThat(bean3.getId()).isNotNull();

    assertThat(DB.find(EBasic.class, bean1.getId())).isNotNull();
    assertThat(DB.find(EBasic.class, bean2.getId())).isNotNull();
    assertThat(DB.find(EBasic.class, bean3.getId())).isNotNull();
  }

  @Test
  void database_returnsTheDatabaseInstance() {
    EBasic saved = builder.save(EBasic.class);

    EBasic found = builder.database().find(EBasic.class, saved.getId());
    assertThat(found).isNotNull();
    assertThat(found.getName()).isEqualTo(saved.getName());
  }

  @Test
  void build_unknownClass_throwsIllegalArgumentException() {
    org.assertj.core.api.Assertions.assertThatThrownBy(() -> builder.build(String.class))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("No BeanDescriptor found");
  }
}
