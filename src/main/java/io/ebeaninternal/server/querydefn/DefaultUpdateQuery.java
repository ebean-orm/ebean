package io.ebeaninternal.server.querydefn;

import io.ebean.ExpressionList;
import io.ebean.ProfileLocation;
import io.ebean.UpdateQuery;

/**
 * Default implementation of UpdateQuery.
 */
public class DefaultUpdateQuery<T> implements UpdateQuery<T> {

  private final OrmUpdateProperties values = new OrmUpdateProperties();

  private final DefaultOrmQuery<T> query;

  public DefaultUpdateQuery(DefaultOrmQuery<T> query) {
    this.query = query;
    query.setUpdateProperties(values);
  }

  @Override
  public UpdateQuery<T> set(String property, Object value) {
    values.set(property, value);
    return this;
  }

  @Override
  public UpdateQuery<T> setNull(String property) {
    values.set(property, null);
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
  public ExpressionList<T> where() {
    return query.where();
  }
}
