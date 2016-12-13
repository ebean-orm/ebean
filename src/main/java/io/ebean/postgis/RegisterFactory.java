package io.ebean.postgis;

import io.ebean.config.ServerConfig;
import io.ebean.plugin.ExtraTypeFactory;
import io.ebean.postgis.latte.ScalarTypeGeoLatteLineString;
import io.ebean.postgis.latte.ScalarTypeGeoLatteMultiLineString;
import io.ebean.postgis.latte.ScalarTypeGeoLatteMultiPoint;
import io.ebean.postgis.latte.ScalarTypeGeoLatteMultiPolygon;
import io.ebean.postgis.latte.ScalarTypeGeoLattePoint;
import io.ebean.postgis.latte.ScalarTypeGeoLattePolygon;
import io.ebeaninternal.server.type.ScalarType;

import java.util.ArrayList;
import java.util.List;

public class RegisterFactory implements ExtraTypeFactory {


  @Override
  public List<ScalarType<?>> createTypes(ServerConfig serverConfig, Object objectMapper) {

    List<ScalarType<?>> list = new ArrayList<ScalarType<?>>();
    list.add(new ScalarTypePgisPoint());
    list.add(new ScalarTypePgisPolygon());
    list.add(new ScalarTypePgisLineString());
    list.add(new ScalarTypePgisMultiPolygon());
    list.add(new ScalarTypePgisMultiPoint());
    list.add(new ScalarTypePgisMultiLineString());

    if (serverConfig.getClassLoadConfig().isPresent("org.geolatte.geom.Geometry")) {
      list.add(new ScalarTypeGeoLattePoint());
      list.add(new ScalarTypeGeoLattePolygon());
      list.add(new ScalarTypeGeoLatteLineString());
      list.add(new ScalarTypeGeoLatteMultiPolygon());
      list.add(new ScalarTypeGeoLatteMultiPoint());
      list.add(new ScalarTypeGeoLatteMultiLineString());
    }

    return list;
  }
}
