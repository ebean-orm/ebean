package io.ebeaninternal.server.deploy;

import io.ebean.bean.EntityBean;
import io.ebean.core.type.DataReader;
import io.ebean.text.TextException;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;

import javax.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * A DbJson property that does not use Jackson ObjectMapper.
 */
public class BeanPropertyJsonBasic extends BeanProperty {

  public BeanPropertyJsonBasic(BeanDescriptor<?> descriptor, DeployBeanProperty deploy) {
    super(descriptor, deploy);
  }

  protected BeanPropertyJsonBasic(BeanProperty source, BeanPropertyOverride override) {
    super(source, override);
  }

  @Override
  public BeanProperty override(BeanPropertyOverride override) {
    return new BeanPropertyJsonBasic(this, override);
  }

  protected Object checkForEmpty(EntityBean bean) {
    final Object value = getValue(bean);
    if (value instanceof Collection && ((Collection<?>) value).isEmpty()
      || value instanceof Map && ((Map<?, ?>) value).isEmpty()) {
      return value;
    }
    return null;
  }

  @Override
  public Object readSet(DataReader reader, EntityBean bean) throws SQLException {
    try {
      Object value = scalarType.read(reader);
      if (value == null) {
        value = checkForEmpty(bean);
      }
      if (bean != null) {
        setValue(bean, value);
      }
      return value;
    } catch (TextException e) {
      throw e;
    } catch (Exception e) {
      throw new PersistenceException("Error readSet on " + descriptor + "." + name, e);
    }
  }

}
