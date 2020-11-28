package io.ebean.postgis;

import io.ebean.config.DatabaseConfig;
import io.ebeaninternal.api.GeoTypeProvider;
import io.ebeaninternal.server.type.GeoTypeBinder;

public class PostgisGeoTypeBindProvider implements GeoTypeProvider {

  @Override
  public GeoTypeBinder createBinder(DatabaseConfig config) {
    boolean withGeolatte = config.getClassLoadConfig().isPresent("org.geolatte.geom.Geometry");
    return new PostgisGeoTypeBinder(withGeolatte);
  }
}
