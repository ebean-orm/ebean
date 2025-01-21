package io.ebeaninternal.server.deploy;

import io.ebean.PersistenceIOException;
import io.ebean.SqlUpdate;
import io.ebean.bean.EntityBean;
import io.ebean.lookup.Lookups;
import io.ebeaninternal.api.json.SpiJsonReader;
import io.ebeaninternal.api.json.SpiJsonWriter;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;

import java.io.IOException;

/**
 * Bean descriptor used with element collection of list/set of embeddable.
 */
@SuppressWarnings("rawtypes")
class BeanDescriptorElementEmbedded<T> extends BeanDescriptorElement<T> {

  private final BeanPropertyAssocOne embeddedProperty;
  private final EntityBean prototype;
  private BeanDescriptor targetDescriptor;

  BeanDescriptorElementEmbedded(BeanDescriptorMap owner, DeployBeanDescriptor<T> deploy, ElementHelp elementHelp) {
    super(owner, deploy, elementHelp);
    try {
      this.prototype = Lookups.newDefaultInstance(beanType);
    } catch (Throwable e) {
      throw new IllegalStateException("Unable to create entity bean prototype for "+beanType);
    }
    BeanPropertyAssocOne<?>[] embedded = propertiesEmbedded();
    if (embedded.length == 1) {
      embeddedProperty = embedded[0];
    } else {
      embeddedProperty = null;
    }
  }

  @Override
  public boolean isJsonReadCollection() {
    return false;
  }

  @Override
  public void initialiseOther(BeanDescriptorInitContext initContext) {
    super.initialiseOther(initContext);
    this.targetDescriptor = embeddedProperty.targetDescriptor();
  }

  @Override
  public EntityBean createEntityBeanForJson() {
    return (EntityBean)prototype._ebean_newInstance();
  }

  @Override
  public void bindElementValue(SqlUpdate insert, Object value) {
    targetDescriptor.bindElementValue(insert, value);
  }

  @Override
  public void jsonWriteElement(SpiJsonWriter ctx, Object element) {
    writeJsonElement(ctx, element);
  }

  @Override
  public T jsonRead(SpiJsonReader jsonRead, String path, T target) throws IOException {
    return readJsonElement(jsonRead, path, target);
  }

  @SuppressWarnings("unchecked")
  T readJsonElement(SpiJsonReader jsonRead, String path, T target) throws IOException {
    return (T)targetDescriptor.jsonRead(jsonRead, path, target);
  }

  void writeJsonElement(SpiJsonWriter ctx, Object element) {
    try {
      if (element == null) {
        ctx.writeNull();
      } else {
        targetDescriptor.jsonWrite(ctx, (EntityBean)element);
      }
    } catch (IOException e) {
      throw new PersistenceIOException(e);
    }
  }
}
