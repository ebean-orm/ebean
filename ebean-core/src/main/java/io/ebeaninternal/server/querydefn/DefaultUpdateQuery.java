package io.ebeaninternal.server.querydefn;

import io.ebean.ExpressionList;
import io.ebean.ProfileLocation;
import io.ebean.Query;
import io.ebean.UpdateQuery;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;

/**
 * Default implementation of UpdateQuery.
 */
public final class DefaultUpdateQuery<T> implements UpdateQuery<T> {

  private final OrmUpdateProperties values = new OrmUpdateProperties();
  private final DefaultOrmQuery<T> query;
  private final BeanDescriptor<T> descriptor;

  public DefaultUpdateQuery(DefaultOrmQuery<T> query) {
    this.query = query;
    this.descriptor = query.descriptor();
    query.setUpdateProperties(values);
  }

  @Override
  public UpdateQuery<T> set(String property, Object value) {
    if (value == null) {
      values.setNull(property);
    } else {
      final BeanProperty beanProperty = descriptor.beanProperty(property);
      final ScalarType<Object> scalarType = (beanProperty == null) ? null: beanProperty.scalarType();
      values.set(property, value, scalarType);
    }
    return this;
  }

  @Override
  public <P> UpdateQuery<T> set(Query.Property<P> property, P value) {
    return set(property.toString(), value);
  }

  @Override
  public UpdateQuery<T> setNull(Query.Property<?> property) {
    return setNull(property.toString());
  }

  @Override
  public UpdateQuery<T> setNull(String property) {
    values.setNull(property);
    return this;
  }

  @Override
  public UpdateQuery<T> setRaw(String propertyExpression) {
    values.setRaw(propertyExpression);
    return this;
  }

  @Override
  public UpdateQuery<T> setRaw(String propertyExpression, Object... vals) {
    values.setRaw(propertyExpression, vals);
    return this;
  }

  @Override
  public UpdateQuery<T> setProfileLocation(ProfileLocation profileLocation) {
    query.setProfileLocation(profileLocation);
    return this;
  }

  @Override
  public UpdateQuery<T> setLabel(String label) {
    query.setLabel(label);
    return this;
  }

  @Override
  public ExpressionList<T> where() {
    return query.where();
  }

  @Override
  public int update() {
    return query.update();
  }
}
