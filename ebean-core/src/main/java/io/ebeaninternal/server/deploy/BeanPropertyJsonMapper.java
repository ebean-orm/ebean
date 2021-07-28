package io.ebeaninternal.server.deploy;

import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.core.type.DataReader;
import io.ebean.text.TextException;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import io.ebeaninternal.server.util.Md5;

import javax.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.Objects;

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
    final String newHash = Md5.hash(json);
    final String oldHash = ebi.mutableHash(propertyIndex);
    if (!Objects.equals(newHash, oldHash)) {
      ebi.mutableContent(propertyIndex, json); // so we only convert to json once
      ebi.mutableHash(propertyIndex, newHash); // for dirty detection next time
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
          final String hash = Md5.hash(json);
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
