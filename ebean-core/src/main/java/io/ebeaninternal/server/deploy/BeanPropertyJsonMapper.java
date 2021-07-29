package io.ebeaninternal.server.deploy;

import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.MutableValueNext;
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
   * Next when no prior MutableValueInfo.
   */
  private MutableValueNext next(String json) {
    if (keepSource) {
      return new SourceMutableValue(scalarType, json);
    } else if (dirtyDetection) {
      return new NextPair(json, new ChecksumMutableValue(scalarType, json));
    } else {
      throw new IllegalStateException("Never get here");
    }
  }

  /**
   * Return true if the mutable value is considered dirty.
   * This is only used for 'mutable' scalar types like hstore etc.
   */
  @Override
  boolean updateMutableValue(Object value, boolean alreadyDirty, EntityBeanIntercept ebi) {
    // dirty detection based on json content or checksum of json content
    // only perform serialisation to json once
    final String json = scalarType.format(value);
    final MutableValueInfo oldHash = ebi.mutableInfo(propertyIndex);
    if (oldHash == null) {
      ebi.mutableNext(propertyIndex, next(json));
      return true;
    }
    // only perform compute of checksum/hash once (if checksum based)
    final MutableValueNext next = oldHash.nextDirty(json);
    if (next != null) {
      ebi.mutableNext(propertyIndex, next);
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

  private static final class NextPair implements MutableValueNext {

    private final String json;
    private final MutableValueInfo next;

    NextPair(String json, MutableValueInfo next) {
      this.json = json;
      this.next = next;
    }

    @Override
    public String content() {
      return json;
    }

    @Override
    public MutableValueInfo info() {
      return next;
    }
  }

  /**
   * Hold checksum of json source content to use for dirty detection.
   * <p>
   * Does not support rebuilding 'oldValue' as no original json content.
   */
  private static final class ChecksumMutableValue implements MutableValueInfo {

    private final ScalarType<?> parent;
    private final long checksum;

    ChecksumMutableValue(ScalarType<?> parent, String json) {
      this.parent = parent;
      this.checksum = Checksum.checksum(json);
    }

    /**
     * Create with pre-computed checksum.
     */
    ChecksumMutableValue(ScalarType<?> parent, long checksum) {
      this.parent = parent;
      this.checksum = checksum;
    }

    @Override
    public MutableValueNext nextDirty(String json) {
      final long nextChecksum = Checksum.checksum(json);
      return nextChecksum == checksum ? null : new NextPair(json, new ChecksumMutableValue(parent, nextChecksum));
    }

    @Override
    public boolean isEqualToObject(Object obj) {
      return Checksum.checksum(parent.format(obj)) == checksum;
    }

    @Override
    public Object get() {
      return null; // cannot create object from json
    }
  }

  /**
   * Hold json source content. This supports rebuilding the 'oldValue'.
   */
  private static final class SourceMutableValue implements MutableValueInfo, MutableValueNext {

    private final String originalJson;
    private final ScalarType<?> parent;

    SourceMutableValue(ScalarType<?> parent, String json) {
      this.parent = parent;
      this.originalJson = json;
    }

    @Override
    public MutableValueNext nextDirty(String json) {
      return Objects.equals(originalJson, json) ? null : new SourceMutableValue(parent, json);
    }

    @Override
    public boolean isEqualToObject(Object obj) {
      return Objects.equals(originalJson, parent.format(obj));
    }

    @Override
    public Object get() {
      // rebuild the 'oldValue' for change log etc
      return parent.parse(originalJson);
    }

    @Override
    public String content() {
      return originalJson;
    }

    @Override
    public MutableValueInfo info() {
      return this;
    }
  }

  /**
   * No dirty detection on json content.
   */
  private static final class NoDirtyDetection implements MutableValueInfo {

    @Override
    public MutableValueNext nextDirty(String json) {
      return null; // treat as not dirty
    }

    @Override
    public boolean isEqualToObject(Object obj) {
      return true; // treat as not dirty
    }
  }
}
