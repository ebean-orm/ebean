package io.ebeaninternal.server.dto;

import io.ebeaninternal.server.type.DataReader;

import java.sql.SQLException;

/**
 * Plan based on Constructor plus some setter methods.
 */
class DtoQueryPlanConPlus extends DtoQueryPlanBase {

  private final DtoMetaConstructor maxArgConstructor;

  private final DtoReadSet[] setterProps;

  DtoQueryPlanConPlus(DtoMappingRequest request, DtoMetaConstructor maxArgConstructor, DtoReadSet[] setterProps) {
    super(request);
    this.maxArgConstructor = maxArgConstructor;
    this.setterProps = setterProps;
  }

  @Override
  public Object readRow(DataReader dataReader) throws SQLException {

    Object bean = maxArgConstructor.process(dataReader);
    for (DtoReadSet setterProp : setterProps) {
      setterProp.readSet(bean, dataReader);
    }
    return bean;
  }

}
