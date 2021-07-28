package io.ebeaninternal.server.deploy;

import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.MutableHash;
import io.ebean.core.type.DataReader;
import io.ebean.text.TextException;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;

import javax.persistence.PersistenceException;
import java.sql.SQLException;

public class BeanPropertyJsonMapper extends BeanProperty {

  public BeanPropertyJsonMapper(BeanDescriptor<?> desc, DeployBeanProperty deployProp) {
    super(desc, deployProp);
  }

  /**
   * Return true if the mutable value is considered dirty.
   * This is only used for 'mutable' scalar types like hstore etc.
   */
  @Override
  boolean isDirtyValue(Object value, EntityBeanIntercept ebi) {
    // dirty detection based on md5 hash of json content
    final String json = scalarType.jsonMapper(value);
    final MutableHash oldHash = ebi.mutableHash(propertyIndex);
    if (oldHash == null || !oldHash.isEqualToJson(json)) {
      ebi.mutableContent(propertyIndex, json); // so we only convert to json once
      return true;
    }
    return false;
  }

  @Override
  public Object readSet(DataReader reader, EntityBean bean) throws SQLException {
    try {
      Object value = scalarType.read(reader);
      if (bean != null) {
        setValue(bean, value);
        String json = reader.popJson();
        if (json != null) {
          final MutableHash hash = scalarType.createMutableHash(json);
          bean._ebean_getIntercept().mutableHash(propertyIndex, hash);
        }
      }
      return value;
    } catch (TextException e) {
      throw e;
    } catch (Exception e) {
      throw new PersistenceException("Error readSet on " + descriptor + "." + name, e);
    }
  }
}
