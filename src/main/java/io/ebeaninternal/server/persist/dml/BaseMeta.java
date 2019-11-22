package io.ebeaninternal.server.persist.dml;

import io.ebeaninternal.api.ConcurrencyMode;
import io.ebeaninternal.server.persist.dmlbind.Bindable;
import io.ebeaninternal.server.persist.dmlbind.BindableId;

class BaseMeta {

  final BindableId id;
  final Bindable version;
  final Bindable tenantId;

  BaseMeta(BindableId id, Bindable version, Bindable tenantId) {
    this.id = id;
    this.version = version;
    this.tenantId = tenantId;
  }

  String appendWhere(GenerateDmlRequest request, ConcurrencyMode conMode) {
    request.setWhereIdMode();
    id.dmlAppend(request);
    if (tenantId != null) {
      tenantId.dmlAppend(request);
    }

    if (ConcurrencyMode.VERSION == conMode) {
      if (version != null) {
        version.dmlAppend(request);
      }
    }

    return request.toString();
  }
}
