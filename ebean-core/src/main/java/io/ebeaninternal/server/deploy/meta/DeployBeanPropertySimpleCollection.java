package io.ebeaninternal.server.deploy.meta;

import io.ebean.bean.BeanCollection.ModifyListenMode;
import io.ebeaninternal.server.deploy.ManyType;

public final class DeployBeanPropertySimpleCollection<T> extends DeployBeanPropertyAssocMany<T> {

  public DeployBeanPropertySimpleCollection(DeployBeanDescriptor<?> desc, Class<T> targetType, ManyType manyType) {
    super(desc, targetType, manyType);
    this.modifyListenMode = ModifyListenMode.ALL;
  }

  /**
   * Returns false as never a ManyToMany.
   */
  @Override
  public boolean isManyToMany() {
    return false;
  }

  /**
   * Returns true as always Unidirectional.
   */
  @Override
  public boolean isUnidirectional() {
    return true;
  }

}
