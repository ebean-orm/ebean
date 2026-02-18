package io.ebeaninternal.server.deploy;

import io.ebean.annotation.MutationDetection;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.MutableValueInfo;
import io.ebean.bean.MutableValueNext;
import io.ebean.bean.PersistenceContext;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.ScalarType;
import io.ebean.text.TextException;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import io.ebeaninternal.server.util.Checksum;

import jakarta.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Handle json property with MutationDetection of SOURCE or HASH only.
 */
public final class BeanPropertyJsonMapper extends BeanPropertyJsonBasic {

  private final boolean sourceDetection;

  public BeanPropertyJsonMapper(BeanDescriptor<?> desc, DeployBeanProperty deployProp) {
    super(desc, deployProp);
    this.sourceDetection = deployProp.getMutationDetection() == MutationDetection.SOURCE;
  }

  private BeanPropertyJsonMapper(BeanPropertyJsonMapper source, BeanPropertyOverride override) {
    super(source, override);
    this.sourceDetection = source.sourceDetection;
  }

  @Override
  public BeanProperty override(BeanPropertyOverride override) {
    return new BeanPropertyJsonMapper(this, override);
  }

  @Override
  public MutableValueInfo createMutableInfo(Object value) {
    if (sourceDetection) {
      return new SourceMutableValue(scalarType, value);
    } else {
      return new ChecksumMutableValue(scalarType, value);
    }
  }

  /**
   * Next when no prior MutableValueInfo.
   */
  private MutableValueNext next(String json, Object value) {
    if (sourceDetection) {
      return new SourceMutableValue(scalarType, value);
    } else {
      return new NextPair(json, new ChecksumMutableValue(scalarType, value));
    }
  }

  /**
   * Return true if the json property is considered dirty.
   */
  @Override
  boolean checkMutable(Object value, boolean alreadyDirty, EntityBeanIntercept ebi) {
    // mutation detection based on json content or checksum of json content
    // only perform serialisation to json once
    final String json = scalarType.format(value);

    final MutableValueInfo oldHash = ebi.mutableInfo(propertyIndex);
    if (oldHash == null) {
      if (value == null) {
        return false; // no change, still null
      }
      ebi.mutableNext(propertyIndex, next(json, value));
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
      if (value == null) {
        value = checkForEmpty(bean);
      }
      if (bean != null) {
        setValue(bean, value);
        if (value != null) {
          final MutableValueInfo hash = createMutableInfo(value);
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

  @Override
  public void setCacheDataValue(EntityBean bean, Object cacheData, PersistenceContext context) {
    if (cacheData instanceof String) {
      // parse back from string to support optimisation of java object serialisation
      cacheData = scalarType.parse((String) cacheData);
      final MutableValueInfo hash = createMutableInfo(cacheData);
      bean._ebean_getIntercept().mutableInfo(propertyIndex, hash);

    }
    setValue(bean, cacheData);
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

    ChecksumMutableValue(ScalarType<?> parent, Object value) {
      this.parent = parent;
      this.checksum = Checksum.checksum(parent.format(value));
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

    private final String formattedJson;
    private final ScalarType<?> parent;

    SourceMutableValue(ScalarType<?> parent, Object value) {
      this.parent = parent;
      this.formattedJson = parent.format(value);
    }

    @Override
    public MutableValueNext nextDirty(String json) {
      return Objects.equals(formattedJson, json) ? null : new SourceMutableValue(parent, parent.parse(json));
    }

    @Override
    public boolean isEqualToObject(Object obj) {
      return Objects.equals(formattedJson, parent.format(obj));
    }

    @Override
    public Object get() {
      // rebuild the 'oldValue' for change log etc
      return parent.parse(formattedJson);
    }

    @Override
    public String content() {
      return formattedJson;
    }

    @Override
    public MutableValueInfo info() {
      return this;
    }
  }
}
