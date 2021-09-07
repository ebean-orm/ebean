package org.tests.model.map;

import io.ebean.DB;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


public class BeanMapOrphanRemovalTest {

  @Test
  public void keySet_retainAll() {

    MpUser user = new MpUser();
    user.setName("u1");
    addRoles(user, "r1", "r2", "r3", "r4");
    DB.save(user);

    final MpUser user1 = DB.find(MpUser.class, user.getId());
    final Map<String, MpRole> roles = user1.getRoles();
    assertThat(roles).hasSize(4);


    final Set<String> keySet = roles.keySet();
    keySet.retainAll(Arrays.asList("r2", "r3"));

    DB.save(user1);

    final MpUser user2 = DB.find(MpUser.class, user.getId());
    final Map<String, MpRole> roles2 = user2.getRoles();
    assertThat(roles2).hasSize(2);
  }

  private void addRoles(MpUser user, String... roles){
    for (String code : roles) {
      MpRole role = newRole(code);
      user.getRoles().put(role.getCode(), role);
    }
  }

  private MpRole newRole(String code) {
    MpRole role = new MpRole();
    role.setCode(code);
    return role;
  }
}
