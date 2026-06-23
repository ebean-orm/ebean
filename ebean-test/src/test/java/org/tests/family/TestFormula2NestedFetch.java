package org.tests.family;

import io.ebean.DB;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.family.ChildPerson;
import org.tests.model.family.GrandParentPerson;
import org.tests.model.family.ParentPerson;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the path based @Formula2 auto-join works not only on the root entity
 * but also when the entity holding the @Formula2 property is loaded as a nested fetch
 * (either a nested *ToMany included in the main query, or a nested *ToOne join).
 */
public class TestFormula2NestedFetch extends BaseTestCase {

  private GrandParentPerson setupFamily() {
    ChildPerson fred = new ChildPerson();
    fred.setName("Fred"); // familyName null -> inherits from parent / grandparent

    ChildPerson julia = new ChildPerson();
    julia.setName("Julia");
    julia.setFamilyName("Baz");

    ChildPerson roland = new ChildPerson();
    roland.setName("Roland"); // familyName null

    ParentPerson maria = new ParentPerson();
    maria.setName("Maria");
    maria.setFamilyName("Bar");
    maria.getChildren().add(fred);
    maria.getChildren().add(julia);

    ParentPerson sandra = new ParentPerson();
    sandra.setName("Sandra"); // familyName null -> inherits "Foo"
    sandra.getChildren().add(roland);

    GrandParentPerson josef = new GrandParentPerson();
    josef.setName("Josef");
    josef.setFamilyName("Foo");
    josef.getChildren().add(maria);
    josef.getChildren().add(sandra);

    DB.save(josef);
    return josef;
  }

  private void cleanup() {
    DB.find(ChildPerson.class).delete();
    DB.find(GrandParentPerson.class).delete();
  }

  @Test
  void formula2_nestedMany_autoJoins() {
    GrandParentPerson josef = setupFamily();
    try {
      LoggedSql.start();

      GrandParentPerson found = DB.find(GrandParentPerson.class)
        .fetch("children", "name, derivedFamilyName")
        .fetch("children.children", "name, derivedFamilyName")
        .setId(josef.getIdentifier())
        .findOne();

      List<String> sql = LoggedSql.stop();

      assertThat(found).isNotNull();

      // ParentPerson (path "children") derivedFamilyName = coalesce(familyName, parent.familyName)
      ParentPerson maria = byName(found.getChildren(), "Maria");
      ParentPerson sandra = byName(found.getChildren(), "Sandra");
      assertThat(maria.getDerivedFamilyName()).isEqualTo("Bar"); // own familyName
      assertThat(sandra.getDerivedFamilyName()).isEqualTo("Foo"); // inherited from grandparent

      // ChildPerson (path "children.children") derivedFamilyName =
      //   coalesce(familyName, parent.familyName, parent.parent.familyName)
      ChildPerson fred = childByName(maria.getChildren(), "Fred");
      ChildPerson julia = childByName(maria.getChildren(), "Julia");
      ChildPerson roland = childByName(sandra.getChildren(), "Roland");
      assertThat(fred.getDerivedFamilyName()).isEqualTo("Bar");   // inherited from parent Maria
      assertThat(julia.getDerivedFamilyName()).isEqualTo("Baz");  // own familyName
      assertThat(roland.getDerivedFamilyName()).isEqualTo("Foo"); // inherited from grandparent

      // the nested ParentPerson formula2 is part of the main grand_parent_person query and
      // its auto-join (children.parent -> grand_parent_person t2) is added to that same sql
      String main = sql.stream().filter(s -> s.contains("from grand_parent_person")).findFirst().orElseThrow();
      assertThat(main).contains("coalesce(t1.family_name, t2.family_name)");
      assertThat(main).contains("left join grand_parent_person t2 on t2.identifier = t1.parent_identifier");

      // the ChildPerson many is loaded as its own query - formula2 auto-joins parent and parent.parent
      String childSql = sql.stream().filter(s -> s.contains("from child_person")).findFirst().orElseThrow();
      assertThat(childSql).contains("coalesce(t0.family_name, t1.family_name, t2.family_name)");
      assertThat(childSql).contains("left join parent_person t1 on t1.identifier = t0.parent_identifier");
      assertThat(childSql).contains("left join grand_parent_person t2 on t2.identifier = t1.parent_identifier");
    } finally {
      cleanup();
    }
  }

  @Test
  void formula2_nestedToOne_autoJoins() {
    setupFamily();
    try {
      LoggedSql.start();

      // ChildPerson root, fetch the parent (ParentPerson) which has a @Formula2 requiring
      // parent.parent -> the nested *ToOne formula2 auto-join must be added to the same sql
      List<ChildPerson> children = DB.find(ChildPerson.class)
        .select("name, derivedFamilyName")
        .fetch("parent", "name, derivedFamilyName")
        .where().eq("name", "Fred")
        .findList();

      List<String> sql = LoggedSql.stop();

      assertThat(children).hasSize(1);
      ChildPerson fred = children.get(0);
      assertThat(fred.getDerivedFamilyName()).isEqualTo("Bar");
      // the fetched parent (Maria) derivedFamilyName resolves via its own auto-joined parent
      assertThat(fred.getParent().getDerivedFamilyName()).isEqualTo("Bar");

      String main = sql.get(0);
      // ChildPerson (t0) formula2 -> parent t1, parent.parent t2
      assertThat(main).contains("coalesce(t0.family_name, t1.family_name, t2.family_name)");
      // nested ParentPerson (t1) formula2 -> parent.parent t2 (reused grand_parent_person alias)
      assertThat(main).contains("coalesce(t1.family_name, t2.family_name)");
      assertThat(main).contains("left join parent_person t1 on t1.identifier = t0.parent_identifier");
      assertThat(main).contains("left join grand_parent_person t2 on t2.identifier = t1.parent_identifier");
    } finally {
      cleanup();
    }
  }

  private static ParentPerson byName(List<ParentPerson> list, String name) {
    return list.stream().filter(p -> name.equals(p.getName())).findFirst().orElseThrow();
  }

  private static ChildPerson childByName(List<ChildPerson> list, String name) {
    return list.stream().filter(p -> name.equals(p.getName())).findFirst().orElseThrow();
  }
}
