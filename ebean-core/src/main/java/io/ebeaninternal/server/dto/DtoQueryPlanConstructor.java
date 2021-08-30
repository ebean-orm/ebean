package io.ebeaninternal.server.dto;

import io.ebean.core.type.DataReader;

import java.sql.SQLException;

/**
 * Plan based on mapping via single constructor only.
 */
final class DtoQueryPlanConstructor extends DtoQueryPlanBase {

  private final DtoMetaConstructor constructor;

  DtoQueryPlanConstructor(DtoMappingRequest request, DtoMetaConstructor constructor) {
    super(request);
    this.constructor = constructor;
  }

  @Override
  public Object readRow(DataReader dataReader) throws SQLException {
    return constructor.process(dataReader);
  }

}
