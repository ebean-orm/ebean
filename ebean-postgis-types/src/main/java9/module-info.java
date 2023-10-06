import io.ebean.core.type.ExtraTypeFactory;
import io.ebeaninternal.api.GeoTypeProvider;

/**
 * Provides GeoTypeProvider and ExtraTypeFactory.
 */
module io.ebean.postgis.types {

  provides ExtraTypeFactory with io.ebean.postgis.PostgisExtraTypeFactory;
  provides GeoTypeProvider with io.ebean.postgis.PostgisGeoTypeBindProvider;

  requires io.ebean.core;
  requires org.postgresql.jdbc;
  requires postgis.jdbc;
  requires geolatte.geom;
  requires com.fasterxml.jackson.core;

}
