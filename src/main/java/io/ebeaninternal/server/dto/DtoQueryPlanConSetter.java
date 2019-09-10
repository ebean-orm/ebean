package io.ebeaninternal.server.dto;

import io.ebeaninternal.server.type.DataReader;

import java.sql.SQLException;

/**
 * Plan based on default constructor and setter methods.
 */
class DtoQueryPlanConSetter extends DtoQueryPlanBase {

  private final DtoMetaConstructor defaultConstructor;

  private final DtoReadSet[] setterProps;

  DtoQueryPlanConSetter(DtoMappingRequest request, DtoMetaConstructor defaultConstructor, DtoReadSet[] setterProps) {
    super(request);
    this.defaultConstructor = defaultConstructor;
    this.setterProps = setterProps;
  }

  @Override
  public Object readRow(DataReader dataReader) throws SQLException {

    Object bean = defaultConstructor.defaultConstructor();
    for (DtoReadSet setterProp : setterProps) {
      setterProp.readSet(bean, dataReader);
    }
    return bean;
  }

}
