package org.example.records;

import org.example.records.query.QUserRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRoleTest {

  final UserRoleId id = new UserRoleId(42, "R7");

  @Test
  void id_equals() {
    assertThat(id).isEqualTo(new UserRoleId(42, "R7"));
  }

  @Test
  void id_notEquals() {
    assertThat(id).isNotEqualTo(new UserRoleId(43, "R7"));
    assertThat(id).isNotEqualTo(new UserRoleId(42, "R8"));
  }

  @Test
  void insert_query() {

    var userRole = new UserRole(id, "hello");
    userRole.save();

    UserRole found = new QUserRole()
      .id.eq(new UserRoleId(42, "R7"))
      .findOne();

    UserRoleId id1 = found.getId();
    assertThat(id1).isEqualTo(id);
    assertThat(id1.userId()).isEqualTo(42);
    assertThat(id1.roleId()).isEqualTo("R7");
  }
}
