package io.ebean.postgis;

import io.ebean.DatabaseBuilder;
import io.ebeaninternal.api.GeoTypeProvider;
import io.ebeaninternal.server.type.GeoTypeBinder;

public class PostgisGeoTypeBindProvider implements GeoTypeProvider {

  @Override
  public GeoTypeBinder createBinder(DatabaseBuilder.Settings config) {
    return new PostgisGeoTypeBinder();
  }
}
