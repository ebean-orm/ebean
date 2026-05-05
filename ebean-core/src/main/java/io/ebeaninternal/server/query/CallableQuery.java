package io.ebeaninternal.server.query;

import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;

/**
 * Base object for making query execution into Callable's.
 */
abstract class CallableQuery<T> {

  final SpiQuery<T> query;
  final SpiEbeanServer server;

  CallableQuery(SpiEbeanServer server, SpiQuery<T> query) {
    this.server = server;
    this.query = query;
  }
}
