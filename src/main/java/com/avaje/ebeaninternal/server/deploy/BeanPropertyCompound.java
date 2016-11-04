package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.text.json.EJson;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertyCompound;
import com.avaje.ebeaninternal.server.el.ElPropertyChainBuilder;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;
import com.avaje.ebeaninternal.server.query.SqlBeanLoad;
import com.avaje.ebeaninternal.server.text.json.ReadJson;
import com.avaje.ebeaninternal.server.text.json.WriteJson;
import com.avaje.ebeaninternal.server.type.CtCompoundProperty;
import com.avaje.ebeaninternal.server.type.CtCompoundPropertyElAdapter;
import com.avaje.ebeaninternal.server.type.CtCompoundType;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Property mapped to an Immutable Compound Value Object.
 * <p>
 * An Immutable Compound Value Object is similar to an Embedded bean but it
 * doesn't require enhancement and MUST be treated as an Immutable type.
 * </p>
 */
public class BeanPropertyCompound extends BeanProperty {

  private final CtCompoundType<?> compoundType;

  private final BeanProperty[] scalarProperties;

  private final LinkedHashMap<String, BeanProperty> propertyMap = new LinkedHashMap<String, BeanProperty>();

  private final LinkedHashMap<String, CtCompoundPropertyElAdapter> nonScalarMap = new LinkedHashMap<String, CtCompoundPropertyElAdapter>();

  /**
   * Create the property.
   */
  public BeanPropertyCompound(BeanDescriptor<?> descriptor, DeployBeanPropertyCompound deploy) {

    super(descriptor, deploy);
    this.compoundType = deploy.getCompoundType();

    BeanPropertyCompoundRoot root = deploy.getFlatProperties();
    this.scalarProperties = root.getScalarProperties();

    for (int i = 0; i < scalarProperties.length; i++) {
      propertyMap.put(scalarProperties[i].getName(), scalarProperties[i]);
    }

    List<CtCompoundProperty> nonScalarPropsList = root.getNonScalarProperties();

    for (int i = 0; i < nonScalarPropsList.size(); i++) {
      CtCompoundProperty ctProp = nonScalarPropsList.get(i);
      CtCompoundPropertyElAdapter adapter = new CtCompoundPropertyElAdapter(ctProp);
      nonScalarMap.put(ctProp.getRelativeName(), adapter);
    }

  }

  @Override
  public void initialise() {
    // do nothing for normal BeanProperty
    if (!isTransient && compoundType == null) {
      String msg = "No cvoInternalType assigned to " + descriptor.getFullName() + "." + getName();
      throw new RuntimeException(msg);
    }
  }

  @Override
  public void setDeployOrder(int deployOrder) {
    this.deployOrder = deployOrder;
    for (CtCompoundPropertyElAdapter adapter : nonScalarMap.values()) {
      adapter.setDeployOrder(deployOrder);
    }
  }

  public ElPropertyValue buildElPropertyValue(String propName, String remainder, ElPropertyChainBuilder chain, boolean propertyDeploy) {

    if (chain == null) {
      chain = new ElPropertyChainBuilder(true, propName);
    }

    // first add this property
    chain.add(this);

    // handle all the rest of the chain handled by the
    // BeanProperty (all depth for nested compound type)
    BeanProperty p = propertyMap.get(remainder);
    if (p != null) {
      return chain.add(p).build();
    }
    CtCompoundPropertyElAdapter elAdapter = nonScalarMap.get(remainder);
    if (elAdapter == null) {
      throw new RuntimeException("property [" + remainder + "] not found in " + getFullBeanName());
    }
    return chain.add(elAdapter).build();
  }

  @Override
  public void appendSelect(DbSqlContext ctx, boolean subQuery) {
    if (!isTransient) {
      for (int i = 0; i < scalarProperties.length; i++) {
        scalarProperties[i].appendSelect(ctx, subQuery);
      }
    }
  }

  public BeanProperty[] getScalarProperties() {
    return scalarProperties;
  }

  @Override
  public Object readSet(DbReadContext ctx, EntityBean bean) throws SQLException {

    Object v = compoundType.read(ctx.getDataReader());
    setValue(bean, v);

    return v;
  }

  /**
   * Read the data from the resultSet effectively ignoring it and returning
   * null.
   */
  @Override
  public Object read(DbReadContext ctx) throws SQLException {
    return compoundType.read(ctx.getDataReader());
  }

  @Override
  public void loadIgnore(DbReadContext ctx) {
    compoundType.loadIgnore(ctx.getDataReader());
  }

  @Override
  public void load(SqlBeanLoad sqlBeanLoad) {
    sqlBeanLoad.load(this);
  }

  @Override
  public Object pathGetNested(Object bean) {
    return bean;
  }

  public void jsonWrite(WriteJson ctx, EntityBean bean) throws IOException {
    if (!jsonSerialize) {
      return;
    }
    Object value = getValueIntercept(bean);
    if (value == null) {
      ctx.writeNullField(name);
    } else {
      ctx.writeFieldName(name);
      compoundType.jsonWrite(ctx, value, name);
    }
  }

  @Override
  public void jsonRead(ReadJson readJson, EntityBean bean) throws IOException {

    if (!jsonDeserialize) {
      return;
    }

    Map<String, Object> map = EJson.parseObject(readJson.getParser());
    if (map == null) {
      setValue(bean, null);
    } else {
      Object objValue = compoundType.jsonConvert(map);
      setValue(bean, objValue);
    }
  }
}
