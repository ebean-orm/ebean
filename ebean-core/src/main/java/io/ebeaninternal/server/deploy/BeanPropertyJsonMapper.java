package io.ebeaninternal.server.deploy;

import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.MutableHash;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.ScalarType;
import io.ebean.text.TextException;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import io.ebeaninternal.server.util.Checksum;

import javax.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.Objects;

public class BeanPropertyJsonMapper extends BeanProperty {

  private static final NoDirtyDetection NO_DIRTY_DETECTION = new NoDirtyDetection();
  private final boolean dirtyDetection;
  private final boolean keepSource;

  public BeanPropertyJsonMapper(BeanDescriptor<?> desc, DeployBeanProperty deployProp) {
    super(desc, deployProp);
    this.dirtyDetection = deployProp.isDirtyDetection();
    this.keepSource = deployProp.isKeepSource();
  }

  @Override
  public MutableHash createMutableHash(String json) {
    if (keepSource) {
      return new JsonMutableHash(scalarType, json);
    } else if (dirtyDetection) {
      return new Md5MutableHash(scalarType, json);
    } else {
      return NO_DIRTY_DETECTION;
    }
  }

  /**
   * Return true if the mutable value is considered dirty.
   * This is only used for 'mutable' scalar types like hstore etc.
   */
  @Override
  boolean isDirtyValue(Object value, EntityBeanIntercept ebi) {
    // dirty detection based on md5 hash of json content
    final String json = scalarType.format(value);
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
          final MutableHash hash = createMutableHash(json);
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

  private static class Md5MutableHash implements MutableHash {

    private final String hash;
    private final ScalarType<?> parent;

    Md5MutableHash(ScalarType<?> parent, String json) {
      this.parent = parent;
      this.hash = hash(json);
    }

    private String hash(String json) {
      return String.valueOf(Checksum.checksum(json));
    }

    @Override
    public boolean isEqualToObject(Object obj) {
      return isEqualToJson(parent.format(obj));
    }

    @Override
    public boolean isEqualToJson(String json) {
      return hash(json).equals(hash);
    }

    @Override
    public Object get() {
      return null; // cannot create object from json
    }

  }

  private static class JsonMutableHash implements MutableHash {

    private final String originalJson;
    private final ScalarType<?> parent;

    JsonMutableHash(ScalarType<?> parent, String json) {
      this.parent = parent;
      this.originalJson = json;
    }

    @Override
    public boolean isEqualToObject(Object obj) {
      return isEqualToJson(parent.format(obj));
    }

    @Override
    public boolean isEqualToJson(String json) {
      return Objects.equals(originalJson, json);
    }

    @Override
    public Object get() {
      return parent.parse(originalJson);
    }

  }

  /**
   * No dirty detection on JSON content.
   */
  private static class NoDirtyDetection implements MutableHash {

    @Override
    public boolean isEqualToJson(String json) {
      return true;
    }

    @Override
    public boolean isEqualToObject(Object obj) {
      return true;
    }
  }
}
