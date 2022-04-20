package io.ebeaninternal.server.deploy.parse;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeployInheritInfoTest {

  @Test
  void addChild_when_DiscriminatorValueIsNull() {
    DeployInheritInfo root = new DeployInheritInfo(Object.class);
    root.addChild(new DeployInheritInfo(Integer.class)); // DiscriminatorValue is null
    root.addChild(new DeployInheritInfo(Short.class)); // DiscriminatorValue is null

    DeployInheritInfo c2 = new DeployInheritInfo(Long.class);
    c2.setDiscriminatorValue("c2");
    root.addChild(c2);

    assertThat(root.children()).hasSize(3);
  }
}
