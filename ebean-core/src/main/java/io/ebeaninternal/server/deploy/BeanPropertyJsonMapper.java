package io.ebeaninternal.server.deploy;

import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.MutableValueInfo;
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
  public MutableValueInfo createMutableInfo(String json) {
    if (keepSource) {
      return new SourceMutableValue(scalarType, json);
    } else if (dirtyDetection) {
      return new ChecksumMutableValue(scalarType, json);
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
    // dirty detection based on json content or checksum of json content
    final String json = scalarType.format(value);
    final MutableValueInfo oldHash = ebi.mutableInfo(propertyIndex);
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
          final MutableValueInfo hash = createMutableInfo(json);
          bean._ebean_getIntercept().mutableInfo(propertyIndex, hash);
        }
      }
      return value;
    } catch (TextException e) {
      throw e;
    } catch (Exception e) {
      throw new PersistenceException("Error readSet on " + descriptor + "." + name, e);
    }
  }

  /**
   * Hold checksum of json source content.
   * <p>
   * Dirty detection based on checksum difference on json form.
   * Does not support rebuilding 'oldValue' as no original json content.
   */
  private static class ChecksumMutableValue implements MutableValueInfo {

    private final ScalarType<?> parent;
    private final long checksum;

    ChecksumMutableValue(ScalarType<?> parent, String json) {
      this.parent = parent;
      this.checksum = checksum(json);
    }

    private long checksum(String json) {
      return Checksum.checksum(json);
    }

    @Override
    public boolean isEqualToObject(Object obj) {
      return isEqualToJson(parent.format(obj));
    }

    @Override
    public boolean isEqualToJson(String json) {
      return checksum(json) == checksum;
    }

    @Override
    public Object get() {
      return null; // cannot create object from json
    }
  }

  /**
   * Hold original json source content. This supports rebuilding the 'oldValue'.
   */
  private static class SourceMutableValue implements MutableValueInfo {

    private final String originalJson;
    private final ScalarType<?> parent;

    SourceMutableValue(ScalarType<?> parent, String json) {
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
      // rebuild the 'oldValue' for change log etc
      return parent.parse(originalJson);
    }
  }

  /**
   * No dirty detection on json content.
   */
  private static class NoDirtyDetection implements MutableValueInfo {

    @Override
    public boolean isEqualToJson(String json) {
      return true; // treat as not dirty
    }

    @Override
    public boolean isEqualToObject(Object obj) {
      return true; // treat as not dirty
    }
  }
}
