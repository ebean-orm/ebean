package org.tests.model.virtualprop;

import io.ebean.bean.EntityBean;
import io.ebean.bean.ExtensionAccessor;
import io.ebean.bean.ExtensionAccessors;
import org.tests.model.basic.AttributeHolder;
import org.tests.model.virtualprop.ext.Extension1;

/**
 * @author Roland Praml, FOCONIS AG
 */
public class BytecodeTest extends VirtualBase implements EntityBean {


  private static final Object xx_ebean_acc_test_model_domain_extend_BEntityBaseAbstract;

  private static final ExtensionAccessors xx_ebean_extension_accessors = null;

  protected static ExtensionAccessors _ebean_extension_accessors
       = new ExtensionAccessors(null, null); // or null
           // if annotated with &#64;EntityExtension(my.pkg.Foo.class, my.pkg.Bar.class)
        private static ExtensionAccessor _ebean_acc_my_pkg_Foo = BytecodeTest.xx_ebean_extension_accessors.add(new Extension1()); // myClass is a prototype

  static {
     xx_ebean_acc_test_model_domain_extend_BEntityBaseAbstract = BytecodeTest.xx_ebean_extension_accessors.add(new Extension1());
  }
  private EntityBean[] _ebean_extension_storage;

  public EntityBean _ebean_getExtension2(ExtensionAccessor accessor) {
    EntityBean ret = this._ebean_extension_storage[accessor.getIndex()];
    if (ret == null) {
      ret = this._ebean_getExtensionAccessors().createInstance(accessor, this);
      this._ebean_extension_storage[accessor.getIndex()] = ret;
    }
    return ret;
  }
}
